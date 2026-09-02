package studio.mevera.imperat;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.mevera.imperat.command.Command;
import studio.mevera.imperat.command.CommandPathway;
import studio.mevera.imperat.command.arguments.Argument;
import studio.mevera.imperat.command.arguments.FlagArgument;
import studio.mevera.imperat.command.suggestions.CompletionArg;
import studio.mevera.imperat.command.tree.Node;
import studio.mevera.imperat.command.tree.projection.CommandTreeProjection;
import studio.mevera.imperat.command.tree.projection.ProjectedFlag;
import studio.mevera.imperat.command.tree.projection.ProjectedNode;
import studio.mevera.imperat.context.ArgumentInput;
import studio.mevera.imperat.context.CommandSource;
import studio.mevera.imperat.context.SuggestionContext;

import java.util.List;
import java.util.Locale;

/**
 * Brigadier-tree builder backed by a {@link CommandTreeProjection}. The
 * projection is built once per command at registration time; this class
 * walks it to emit the corresponding Brigadier {@link CommandNode} graph.
 *
 * <p>Behaviourally equivalent to the previous monolithic walker — the
 * projection takes over the per-scope pathway / flag resolution that was
 * previously inlined here, leaving this class to focus on Brigadier-shape
 * translation.</p>
 */
@SuppressWarnings("unchecked")
public abstract non-sealed class BaseBrigadierManager<S extends CommandSource> implements BrigadierManager<S> {

    protected final Imperat<S> dispatcher;

    protected BaseBrigadierManager(Imperat<S> dispatcher) {
        this.dispatcher = dispatcher;
    }

    private static <BS> LiteralCommandNode<BS> cloneWithDiffName(
            LiteralCommandNode<BS> brigOriginalNode,
            String newName
    ) {
        var clone = new LiteralCommandNode<>(newName,
                brigOriginalNode.getCommand(), brigOriginalNode.getRequirement(), brigOriginalNode.getRedirect(),
                brigOriginalNode.getRedirectModifier(), brigOriginalNode.isFork());
        for (var child : brigOriginalNode.getChildren()) {
            clone.addChild(child);
        }
        return clone;
    }

    /**
     * Per-{@link #parseCommandIntoNode} memo of built nodes so the emitted
     * Brigadier graph is a DAG rather than a tree of copies. A child scope is
     * reachable from its parent anchor AND after every optional argument —
     * without sharing, each attach point would rebuild the entire child
     * subtree (plus alias clones), multiplying node count by
     * (optionals + 1) at every level of the tree packet sent to clients.
     *
     * <p>Keys are {@link ProjectedNode} instances (identity — the projection
     * is built once per registration). Sharing is safe because the duplicate
     * subtrees were structurally identical: same requirement closures, same
     * suggestion providers, same flag redirects.</p>
     */
    private static final class BuildCache<BS> {
        final java.util.Map<Object, java.util.List<CommandNode<BS>>> childNodes = new java.util.IdentityHashMap<>();
    }

    @Override
    public @NotNull <BS> LiteralCommandNode<BS> parseCommandIntoNode(@NotNull Command<S> command) {
        CommandTreeProjection<S> projection = CommandTreeProjection.of(command);
        return this.buildRoot(command, projection.root(), new BuildCache<>());
    }

    private <BS> LiteralCommandNode<BS> buildRoot(Command<S> rootCommand, ProjectedNode<S> root, BuildCache<BS> cache) {
        Command<S> rootCmdLit = root.mainArgument().asCommand();
        LiteralArgumentBuilder<BS> builder = (LiteralArgumentBuilder<BS>)
                                                     literal(rootCmdLit.getName())
                    .requires((obj) -> {
                        var source = wrapCommandSource(obj);
                        return rootCmdLit.isIgnoringACPerms()
                                       || dispatcher.config().getPermissionChecker().hasPermission(source, rootCmdLit);
                    });
        executor(builder);
        appendContinuations(rootCommand, root, builder, 0, cache);
        LiteralCommandNode<BS> rootNode = builder.build();
        appendFlagNode(rootCommand, root, rootNode);
        return rootNode;
    }

    private <BS> CommandNode<BS> convertProjectedNode(
            Command<S> rootCommand,
            ProjectedNode<S> projected,
            BuildCache<BS> cache
    ) {
        Argument<S> main = projected.mainArgument();

        // SimpleArgumentType / similar fixed-arity Imperat types declare
        // their token count via {@code getNumberOfParametersToConsume}.
        // Brigadier requires one node per consumed token for the client-side
        // tree to render the input as N segments — a single Brigadier node
        // would only consume the first whitespace-separated token and paint
        // the rest gray/red. Detect the count and chain N-1 string-typed
        // filler nodes after the head; the deepest node owns the executor
        // and continuations so children/optionals/flags only surface after
        // the user has typed all N tokens.
        int tokenCount = tokenCountOf(main);
        if (tokenCount > 1) {
            return chainMultiTokenNode(rootCommand, projected, tokenCount, cache);
        }

        boolean isGreedy = main.isGreedy() || main.type().isGreedy(main);

        ArgumentBuilder<BS, ?> childBuilder = createBrigadierBuilder(rootCommand, projected);
        executor(childBuilder);
        if (!main.isCommand()) {
            ((RequiredArgumentBuilder<BS, ?>) childBuilder).suggests(
                    isGreedy
                            ? createGreedyDelegateProvider(rootCommand)
                            : createSuggestionProvider(rootCommand, main)
            );
        }

        if (!isGreedy) {
            appendContinuations(rootCommand, projected, childBuilder, 0, cache);
        }
        CommandNode<BS> scopeAnchor = childBuilder.build();
        if (!isGreedy) {
            appendFlagNode(rootCommand, projected, scopeAnchor);
        }
        return scopeAnchor;
    }

    /**
     * Splits {@code argument.format()} on whitespace after stripping the
     * angle / square brackets ({@code <}, {@code >}, {@code [}, {@code ]})
     * and returns the part names if and only if the count matches
     * {@code expectedCount} exactly. {@code null} otherwise — callers fall
     * back to the auto-generated {@code _partN} naming scheme.
     *
     * <p>Lets users opt into named multi-token Brigadier segments by
     * authoring a {@code @Format} like {@code "<chunkX chunkZ>"} — the
     * client then renders the chain as {@code <chunkX> <chunkZ>} instead
     * of {@code <chunk> <chunk_part2>}.</p>
     */
    private @Nullable String[] derivePartNamesFromFormat(Argument<S> argument, int expectedCount) {
        String format = argument.format();
        if (format == null || format.isBlank()) {
            return null;
        }
        String cleaned = format
                                 .replace("<", "")
                                 .replace(">", "")
                                 .replace("[", "")
                                 .replace("]", "")
                                 .trim();
        if (!cleaned.contains(" ")) {
            return null;
        }
        String[] parts = cleaned.split("\\s+");
        if (parts.length != expectedCount) {
            return null;
        }
        for (String part : parts) {
            if (part.isBlank()) {
                return null;
            }
        }
        return parts;
    }

    /**
     * Returns the fixed-arity token count for {@code argument}, or {@code 1}
     * for command literals, greedy types, and any type whose
     * {@link studio.mevera.imperat.command.arguments.type.ArgumentType#getNumberOfParametersToConsume}
     * throws — those don't fit the chain-of-nodes shape (greedy/complex
     * types stay as a single Brigadier node and consume their own tokens
     * server-side via {@link #executor}).
     */
    private int tokenCountOf(Argument<S> argument) {
        if (argument.isCommand() || argument.isGreedy()) {
            return 1;
        }
        try {
            return Math.max(1, argument.type().getNumberOfParametersToConsume(argument));
        } catch (Throwable ignored) {
            return 1;
        }
    }

    /**
     * Builds the N-token chain for a fixed-arity positional argument:
     * head node carrying the proper Brigadier {@code ArgumentType} +
     * {@code tokenCount - 1} string-typed filler nodes. Each node is
     * independently {@link #executor executable} so partial-input
     * dispatch still routes through Imperat (the user gets a typed
     * parse error rather than a Brigadier syntax fail). Suggestions on
     * every node delegate to the same Imperat-side suggester so client
     * autocomplete reads the same list at every segment. Only the
     * DEEPEST node receives {@link #appendContinuations} — children,
     * optionals, and flags reachable from the projected scope come
     * after all N tokens, matching how Imperat's tree consumes them.
     *
     * <p>Per-segment naming: if {@link #derivePartNamesFromFormat}
     * yields N parts the chain uses those (head → parts[0], filler[i]
     * → parts[i+1]). Otherwise head keeps {@code argument.getName()}
     * and fillers fall back to the auto-generated {@code _partN}
     * scheme.</p>
     */
    private <BS> CommandNode<BS> chainMultiTokenNode(
            Command<S> rootCommand,
            ProjectedNode<S> projected,
            int tokenCount,
            BuildCache<BS> cache
    ) {
        Argument<S> main = projected.mainArgument();
        String[] partNames = derivePartNamesFromFormat(main, tokenCount);
        java.util.function.Predicate<Object> visibility =
                (obj) -> isNodeVisible(rootCommand, projected, wrapCommandSource(obj));

        String headName = partNames != null ? partNames[0] : main.getName();
        RequiredArgumentBuilder<BS, ?> head = RequiredArgumentBuilder.argument(headName, getArgumentType(main));
        head.requires(visibility::test);
        head.suggests(createSuggestionProvider(rootCommand, main));
        executor(head);

        List<ArgumentBuilder<BS, ?>> fillers = createStringTokenFillers(
                rootCommand, main, tokenCount, visibility, partNames
        );
        ArgumentBuilder<BS, ?> deepest = fillers.isEmpty() ? head : fillers.getLast();
        appendContinuations(rootCommand, projected, deepest, 0, cache);
        return buildTokenChain(head, fillers, (deepestNode) -> appendFlagNode(rootCommand, projected, deepestNode));
    }

    /**
     * Creates the {@code tokenCount - 1} string-typed filler builders that
     * follow the head node, in head→deepest order (the list is empty for
     * single-token arguments). Used by both the positional-arg path
     * ({@link #chainMultiTokenNode}) and the optional-arg path
     * ({@link #appendOptionalContinuation}) so fixed-arity Imperat types
     * render with one Brigadier node per consumed token regardless of
     * where they appear. Each filler shares the head's visibility
     * predicate + the same Imperat-side suggester so autocomplete reads
     * the same list at every segment.
     *
     * <p>The builders are deliberately NOT linked here: Brigadier's
     * {@link ArgumentBuilder#then(ArgumentBuilder)} builds its argument
     * immediately, so linking before continuations are attached would
     * freeze every filler as a childless snapshot. Linking happens
     * bottom-up in {@link #buildTokenChain} once the deepest builder is
     * fully populated.</p>
     *
     * <p>Filler naming: if {@code partNames} is non-null filler {@code i}
     * (1-indexed) uses {@code partNames[i]}. Otherwise the auto-generated
     * {@code <argName>_part<i+1>} scheme is used.</p>
     */
    private <BS> List<ArgumentBuilder<BS, ?>> createStringTokenFillers(
            Command<S> rootCommand,
            Argument<S> argument,
            int tokenCount,
            java.util.function.Predicate<Object> visibility,
            @Nullable String[] partNames
    ) {
        List<ArgumentBuilder<BS, ?>> fillers = new java.util.ArrayList<>(Math.max(0, tokenCount - 1));
        for (int i = 1; i < tokenCount; i++) {
            String fillerName = partNames != null
                                        ? partNames[i]
                                        : argument.getName() + "_part" + (i + 1);
            RequiredArgumentBuilder<BS, ?> filler = RequiredArgumentBuilder.argument(fillerName, getStringArgType(argument));
            filler.requires(visibility::test);
            filler.suggests(createSuggestionProvider(rootCommand, argument));
            executor(filler);
            fillers.add(filler);
        }
        return fillers;
    }

    /**
     * Builds a head + filler chain bottom-up and returns the built head
     * node. Bottom-up is required because {@code then(builder)} snapshots
     * the builder at call time — a top-down link would emit fillers whose
     * own children (deeper fillers, continuations, flags) were added after
     * the snapshot and are therefore lost.
     *
     * @param deepestVisitor invoked with the deepest BUILT node (the head
     *                       itself when there are no fillers) before it is
     *                       attached to its parent, so flags can redirect
     *                       back to the real scope anchor.
     */
    private <BS> CommandNode<BS> buildTokenChain(
            ArgumentBuilder<BS, ?> head,
            List<ArgumentBuilder<BS, ?>> fillers,
            @Nullable java.util.function.Consumer<CommandNode<BS>> deepestVisitor
    ) {
        CommandNode<BS> built = null;
        for (int i = fillers.size() - 1; i >= 0; i--) {
            ArgumentBuilder<BS, ?> filler = fillers.get(i);
            if (built != null) {
                filler.then(built);
            }
            built = filler.build();
            if (i == fillers.size() - 1 && deepestVisitor != null) {
                deepestVisitor.accept(built);
            }
        }
        if (built != null) {
            head.then(built);
        }
        CommandNode<BS> headNode = head.build();
        if (fillers.isEmpty() && deepestVisitor != null) {
            deepestVisitor.accept(headNode);
        }
        return headNode;
    }

    private <BS> ArgumentBuilder<BS, ?> createBrigadierBuilder(
            Command<S> rootCommand,
            ProjectedNode<S> projected
    ) {
        Argument<S> argument = projected.mainArgument();
        ArgumentBuilder<BS, ?> builder = argument.isCommand()
                                                 ? LiteralArgumentBuilder.literal(argument.asCommand().getName())
                                                 : RequiredArgumentBuilder.argument(argument.getName(), getArgumentType(argument));

        builder.requires((obj) -> isNodeVisible(rootCommand, projected, wrapCommandSource(obj)));
        return builder;
    }

    private boolean isNodeVisible(Command<S> rootCommand, ProjectedNode<S> projected, S source) {
        Argument<S> argument = projected.mainArgument();
        if (argument.isCommand() && argument.asCommand().isSecret()) {
            return false;
        }

        if (rootCommand.isIgnoringACPerms() || (argument.isCommand() && argument.asCommand().isIgnoringACPerms())) {
            return true;
        }

        var checker = dispatcher.config().getPermissionChecker();
        if (!argument.isCommand()) {
            return checker.hasPermission(source, projected.originalPathway())
                           && checker.hasPermission(source, argument);
        }

        // Command literal: a grafted subcommand root keeps the subcommand's
        // synthetic default-pathway as its originalPathway, so a method-level
        // @Permission lives only on the TERMINAL pathway. Mirror the core
        // TreeSuggester's visibility walk: the literal is visible only if at
        // least one executable pathway beneath it is permitted.
        return checker.hasPermission(source, argument)
                       && hasVisibleExecutablePathway(projected.sourceNode(), source);
    }

    private boolean hasVisibleExecutablePathway(Node<S> node, S source) {
        var checker = dispatcher.config().getPermissionChecker();
        boolean hasExecutableTerminal = false;
        for (CommandPathway<S> pathway : node.getTerminalPathways()) {
            if (!pathway.isExecutable()) {
                continue;
            }
            hasExecutableTerminal = true;
            if (checker.hasPermission(source, pathway)) {
                return true;
            }
        }

        if (hasExecutableTerminal) {
            return false;
        }

        for (Node<S> child : node.getChildren()) {
            if (hasVisibleExecutablePathway(child, source)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Adds optional continuations and child nodes to {@code parentBuilder}.
     * Flags are NOT registered here — they are added post-build via
     * {@link #appendFlagNode} so the flag-value node can redirect back to
     * the already-built scope anchor node.
     */
    private <BS> void appendContinuations(
            Command<S> rootCommand,
            ProjectedNode<S> scope,
            ArgumentBuilder<BS, ?> parentBuilder,
            int optionalIndex,
            BuildCache<BS> cache
    ) {
        appendOptionalContinuation(rootCommand, scope, parentBuilder, optionalIndex, cache);
        appendChildContinuations(rootCommand, scope, parentBuilder, cache);
    }

    /**
     * Suggestion provider for the {@code <flag>} node: delegates to the
     * Imperat tree suggester with the full input context and keeps only
     * flag-shaped entries ({@code -name}, {@code --name}, inline
     * {@code -name=value}). The core suggester owns used-flag filtering,
     * per-pathway scoping, and permission checks — this provider adds
     * nothing on top, so Brigadier and the core suggester can never
     * disagree about which flags are offered.
     */
    private @NotNull <BS> com.mojang.brigadier.suggestion.SuggestionProvider<BS>
    createFlagSuggestionProvider(Command<S> command) {
        return (context, builder) -> {
            SuggestionContext<S> ctx = createSuggestionContext(command, context.getSource(), context.getInput(), builder, null);
            CompletionArg arg = ctx.getArgToComplete();
            var alignedBuilder = alignToResolvedStart(builder, context.getInput(), arg);
            for (String suggestion : command.tree().tabComplete(ctx)) {
                if (suggestion == null || suggestion.isEmpty()) {
                    continue;
                }
                if (suggestion.startsWith("-")) {
                    alignedBuilder.suggest(suggestion);
                }
            }
            return alignedBuilder.buildFuture();
        };
    }

    /**
     * Suggestion provider for the {@code <flag_value>} node — the position
     * right after a flag token. Delegates to the Imperat tree suggester
     * UNFILTERED: after a value flag the core returns that flag's value
     * completions; after a switch (which consumes no value) it returns the
     * remaining flags and positional/greedy suggestions for this position.
     */
    private @NotNull <BS> com.mojang.brigadier.suggestion.SuggestionProvider<BS>
    createFlagValueDelegateProvider(Command<S> command) {
        return (context, builder) -> {
            SuggestionContext<S> ctx = createSuggestionContext(command, context.getSource(), context.getInput(), builder, null);
            CompletionArg arg = ctx.getArgToComplete();
            var alignedBuilder = alignToResolvedStart(builder, context.getInput(), arg);
            for (String suggestion : command.tree().tabComplete(ctx)) {
                if (suggestion != null && !suggestion.isEmpty()) {
                    alignedBuilder.suggest(suggestion);
                }
            }
            return alignedBuilder.buildFuture();
        };
    }

    /**
     * Suggestion provider for a greedy positional node. Delegates UNFILTERED
     * to the Imperat tree suggester, which at a greedy position returns both
     * the greedy argument's own completions AND the scope's still-available
     * flag names (the flag names the skipped cyclic {@code <flag>} node would
     * otherwise have offered). This keeps {@code -flag} suggested before / at
     * greedy text while avoiding the greedy-sibling cycle Paper rejects.
     */
    private @NotNull <BS> com.mojang.brigadier.suggestion.SuggestionProvider<BS>
    createGreedyDelegateProvider(Command<S> command) {
        return (context, builder) -> {
            SuggestionContext<S> ctx = createSuggestionContext(command, context.getSource(), context.getInput(), builder, null);
            CompletionArg arg = ctx.getArgToComplete();
            var alignedBuilder = alignToResolvedStart(builder, context.getInput(), arg);
            for (String suggestion : command.tree().tabComplete(ctx)) {
                if (suggestion != null && !suggestion.isEmpty()) {
                    alignedBuilder.suggest(suggestion);
                }
            }
            return alignedBuilder.buildFuture();
        };
    }

    /**
     * True when {@code scope} has a greedy positional descendant — a
     * direct child OR reachable through a nested positional chain (e.g.
     * {@code <target> <greedy>}).
     * <p>
     * Such a scope folds its flags into the
     * greedy node's suggester instead of emitting the cyclic
     * {@code <flag>} node.
     * <p>
     * Emitting the flag pair beside any node of a
     * greedy-bearing path is a shape modern Paper's client mirror cannot
     * serialize.
     * <p>
     * It silently drops ASK_SERVER for the whole scope, so
     * the server still computes suggestions but the client never shows
     * them.
     */
    private boolean scopeHasGreedyChild(ProjectedNode<S> scope) {
        for (ProjectedNode<S> child : scope.children()) {
            if (isOrHasGreedy(child)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOrHasGreedy(ProjectedNode<S> node) {
        Argument<S> main = node.mainArgument();
        if (!main.isCommand() && (main.isGreedy() || main.type().isGreedy(main))) {
            return true;
        }
        if (main.isCommand()) {
            // A nested subcommand is its own serialization branch: a greedy
            // inside it does not share the position chain with this scope's
            // flags, so it must not fold this scope's flags away.
            return false;
        }
        for (ProjectedNode<S> child : node.children()) {
            if (isOrHasGreedy(child)) {
                return true;
            }
        }
        return false;
    }

    private <BS> void appendOptionalContinuation(
            Command<S> rootCommand,
            ProjectedNode<S> scope,
            ArgumentBuilder<BS, ?> parentBuilder,
            int optionalIndex,
            BuildCache<BS> cache
    ) {
        List<Argument<S>> optionals = scope.optionalArguments();
        if (optionalIndex >= optionals.size()) {
            return;
        }

        Argument<S> optional = optionals.get(optionalIndex);
        int tokenCount = tokenCountOf(optional);
        String[] partNames = tokenCount > 1
                                     ? derivePartNamesFromFormat(optional, tokenCount)
                                     : null;
        java.util.function.Predicate<Object> visibility =
                (obj) -> isArgumentVisible(rootCommand, optional, scope.originalPathway(), wrapCommandSource(obj));

        // Use the format-derived head name when available so multi-token
        // optionals match their declared format ([chunkX chunkZ] →
        // [chunkX]). Falls back to the argument's own name for the
        // single-token case + the auto-generated {@code _partN} scheme
        // when the format doesn't split evenly.
        String headName = partNames != null ? partNames[0] : optional.getName();
        RequiredArgumentBuilder<BS, ?> optionalBuilder =
                RequiredArgumentBuilder.argument(headName, getArgumentType(optional));
        optionalBuilder.requires(visibility::test);
        optionalBuilder.suggests(createSuggestionProvider(rootCommand, optional));
        executor(optionalBuilder);

        // Chain string-typed filler nodes for fixed-arity types whose
        // {@code getNumberOfParametersToConsume} > 1, so multi-token
        // optionals render as N segments client-side too. Continuation
        // attaches to the deepest filler so the next optional / sibling
        // arrives only after all N tokens.
        List<ArgumentBuilder<BS, ?>> fillers = createStringTokenFillers(
                rootCommand, optional, tokenCount, visibility, partNames
        );
        ArgumentBuilder<BS, ?> deepest = fillers.isEmpty()
                                                 ? optionalBuilder
                                                 : fillers.getLast();

        appendContinuations(rootCommand, scope, deepest, optionalIndex + 1, cache);
        parentBuilder.then(buildTokenChain(optionalBuilder, fillers, null));
    }

    /**
     * Attaches each child scope's node (plus its alias clones) to
     * {@code parentBuilder}. Built nodes are memoized in {@code cache} so
     * every attach point — the scope anchor and each optional-argument
     * depth — shares the SAME node instances instead of rebuilding the
     * subtree per attach point.
     */
    private <BS> void appendChildContinuations(
            Command<S> rootCommand,
            ProjectedNode<S> scope,
            ArgumentBuilder<BS, ?> parentBuilder,
            BuildCache<BS> cache
    ) {
        for (ProjectedNode<S> child : scope.children()) {
            java.util.List<CommandNode<BS>> built = cache.childNodes.get(child);
            if (built == null) {
                built = new java.util.ArrayList<>();
                CommandNode<BS> childBrigNode = this.convertProjectedNode(rootCommand, child, cache);
                built.add(childBrigNode);

                Argument<S> childArgument = child.mainArgument();
                if (childArgument.isCommand()) {
                    for (String alias : childArgument.asCommand().aliases()) {
                        built.add(cloneWithDiffName((LiteralCommandNode<BS>) childBrigNode, alias));
                    }
                }
                cache.childNodes.put(child, built);
            }
            for (CommandNode<BS> node : built) {
                parentBuilder.then(node);
            }
        }
    }

    private boolean isArgumentVisible(
            Command<S> rootCommand,
            Argument<S> argument,
            @Nullable CommandPathway<S> pathway,
            S source
    ) {
        if (argument.isCommand() && argument.asCommand().isSecret()) {
            return false;
        }

        if (rootCommand.isIgnoringACPerms() || (argument.isCommand() && argument.asCommand().isIgnoringACPerms())) {
            return true;
        }

        var checker = dispatcher.config().getPermissionChecker();
        return (pathway == null || checker.hasPermission(source, pathway))
                       && checker.hasPermission(source, argument);
    }

    /**
     * Adds the per-scope flag machinery to {@code scopeAnchor} (the
     * already-built scope node): a single {@code <flag>} argument node whose
     * {@link FlagTokenArgumentType} consumes any flag-shaped token (bare or
     * inline {@code =}-form), with a {@code <flag_value>} child that
     * redirects back to {@code scopeAnchor}. The same node pair is also
     * attached at every optional-argument depth of the scope so flags stay
     * reachable after optionals.
     *
     * <p>Flags are argument nodes, NOT literals, on purpose. Literal
     * completion happens client-side from the synced tree — the server is
     * never asked, so a used flag could not be filtered out of literal
     * suggestions (and structural workarounds cost exponential node counts).
     * With argument nodes every flag suggestion is server-driven and
     * delegates to the core Imperat tree suggester, which already implements
     * the desired semantics: flags offered before greedy text, used flags
     * never re-suggested, value completion per flag.</p>
     *
     * <p>The value node's redirect creates a finite cycle — after a flag
     * (and its value) the parser returns to the scope anchor and can consume
     * another flag or a positional arg, with 2 nodes per scope regardless of
     * flag count.</p>
     */
    private <BS> void appendFlagNode(
            Command<S> command,
            ProjectedNode<S> scope,
            CommandNode<BS> scopeAnchor
    ) {
        if (scope.flags().isEmpty()) {
            return;
        }
        // A greedy positional child folds this scope's flags into its own
        // (core-delegated) suggester. Emitting the cyclic <flag>/<flag_value>
        // pair beside a greedyString node is a shape modern Paper's client
        // mirror cannot serialize — it silently drops ASK_SERVER for the whole
        // scope. Skip the flag nodes here; the greedy node covers flag names.
        if (scopeHasGreedyChild(scope)) {
            return;
        }
        com.mojang.brigadier.arguments.ArgumentType<?> flagType = flagTokenArgumentType();
        if (flagType == null) {
            // Backend opted out — flags won't parse client-side (render red)
            // but completions still flow through sibling providers that
            // delegate to the Imperat tree, and execution parses server-side.
            return;
        }

        java.util.function.Predicate<Object> anyFlagVisible = (obj) -> {
            S source = wrapCommandSource(obj);
            if (command.isIgnoringACPerms()) {
                return true;
            }
            for (ProjectedFlag<S> flag : scope.flags()) {
                if (isFlagVisible(command, flag, source)) {
                    return true;
                }
            }
            return false;
        };

        RequiredArgumentBuilder<BS, ?> valueBuilder =
                RequiredArgumentBuilder.argument("flag_value", flagValueArgumentType());
        valueBuilder.requires(anyFlagVisible::test);
        valueBuilder.suggests(createFlagValueDelegateProvider(command));
        executor(valueBuilder);
        valueBuilder.redirect(scopeAnchor);

        RequiredArgumentBuilder<BS, ?> flagBuilder = RequiredArgumentBuilder.argument("flag", flagType);
        flagBuilder.requires(anyFlagVisible::test);
        flagBuilder.suggests(createFlagSuggestionProvider(command));
        executor(flagBuilder);
        flagBuilder.then(valueBuilder.build());

        CommandNode<BS> flagNode = flagBuilder.build();
        scopeAnchor.addChild(flagNode);
        attachToOptionalChain(scopeAnchor, scope, flagNode);
    }

    /**
     * Attaches {@code node} to each optional-argument attach point of the
     * built scope subtree — the deepest filler node of every optional in
     * declaration order (mirroring where {@link #appendContinuations}
     * attaches continuations during the builder phase). Walks by the same
     * names {@link #appendOptionalContinuation} generated.
     */
    private <BS> void attachToOptionalChain(
            CommandNode<BS> scopeAnchor,
            ProjectedNode<S> scope,
            CommandNode<BS> node
    ) {
        CommandNode<BS> current = scopeAnchor;
        for (Argument<S> optional : scope.optionalArguments()) {
            int tokenCount = tokenCountOf(optional);
            String[] partNames = tokenCount > 1
                                         ? derivePartNamesFromFormat(optional, tokenCount)
                                         : null;
            String headName = partNames != null ? partNames[0] : optional.getName();
            CommandNode<BS> next = current.getChild(headName);
            if (next == null) {
                return;
            }
            for (int i = 1; i < tokenCount; i++) {
                String fillerName = partNames != null
                                            ? partNames[i]
                                            : optional.getName() + "_part" + (i + 1);
                next = next.getChild(fillerName);
                if (next == null) {
                    return;
                }
            }
            next.addChild(node);
            current = next;
        }
    }

    protected @NotNull <BS> SuggestionProvider<BS> createSuggestionProvider(
            Command<S> command,
            Argument<S> parameter
    ) {

        return (context, builder) -> {
            SuggestionContext<S> ctx = createSuggestionContext(command, context.getSource(), context.getInput(), builder, parameter);
            CompletionArg arg = ctx.getArgToComplete();

            String paramFormat = parameter.format();
            Message tooltip = new LiteralMessage(paramFormat);

            // Realign suggestions to the actual arg-token start so the
            // client renders them at the cursor instead of overwriting
            // earlier tokens. Brigadier's default builder.start can be the
            // start of an earlier node (e.g. the literal) when parse picks
            // a different sibling — without this, suggestions silently get
            // dropped client-side because their replace-range is wrong.
            var alignedBuilder = alignToResolvedStart(builder, context.getInput(), arg);
            String prefix = arg.isEmpty() ? "" : arg.value().toLowerCase(Locale.ROOT);

            // Inline-flag partial (`-name=` / `-name=partial`) is structurally
            // a single token, so it falls into whichever ArgumentNode parses
            // any string — usually a sibling positional. The positional's
            // own suggester has no idea how to complete a flag value, so we
            // delegate to Imperat's tree suggester which DOES handle the
            // inline form natively (single-node flag completion).
            String currentToken = arg.value();
            if (currentToken != null
                        && currentToken.indexOf('=') >= 0
                        && studio.mevera.imperat.util.Patterns.isInputFlag(currentToken)) {
                List<String> inline = command.tree().tabComplete(ctx);
                for (String suggestion : inline) {
                    if (suggestion == null || suggestion.isEmpty()) {
                        continue;
                    }
                    // No tooltip on purpose: the inline-flag catch-all
                    // sibling node emits the same texts without one, and
                    // Brigadier's merge only dedups suggestions that are
                    // FULLY equal (text + range + tooltip). A tooltip here
                    // would surface every inline value twice client-side.
                    alignedBuilder.suggest(suggestion);
                }
                return alignedBuilder.buildFuture();
            }

            return dispatcher.config().getParameterSuggestionResolver(parameter).provideAsynchronously(ctx, parameter)
                           .thenCompose((results) -> {
                               if (results.isEmpty()) {
                                   // No custom suggestions — use the argument format
                                   // as a placeholder hint (e.g. `<target>`).
                                   alignedBuilder.suggest(paramFormat);
                               } else {
                                   results
                                           .stream()
                                           .filter((candidate) ->
                                                   prefix.isEmpty() || candidate.toLowerCase(Locale.ROOT).startsWith(prefix))
                                           .forEachOrdered((result) -> alignedBuilder.suggest(result, tooltip));
                               }
                               return alignedBuilder.buildFuture();
                           });
        };
    }

    private boolean isFlagVisible(Command<S> rootCommand, ProjectedFlag<S> flag, S source) {
        if (rootCommand.isIgnoringACPerms()) {
            return true;
        }
        var checker = dispatcher.config().getPermissionChecker();
        return checker.hasPermission(source, flag.owningPathway())
                       && checker.hasPermission(source, flag.flag());
    }

    private @NotNull SuggestionContext<S> createSuggestionContext(
            Command<S> command,
            Object rawSource,
            String rawInput,
            @Nullable SuggestionsBuilder builder,
            @Nullable Argument<S> parameter
    ) {
        if (parameter == null) {
            parameter = findActiveGreedyArgument(command, rawInput, builder);
        }
        S source = wrapCommandSource(rawSource);
        String input = normalizeInput(rawInput);
        int firstSpaceIndex = input.indexOf(' ');
        String label = firstSpaceIndex == -1 ? input : input.substring(0, firstSpaceIndex);
        boolean endsWithSpace = !input.isEmpty() && Character.isWhitespace(input.charAt(input.length() - 1));
        int argumentsStart = firstSpaceIndex == -1 ? input.length() : firstSpaceIndex + 1;

        ArgumentInput args;
        // A flag-shaped token at the tail of the input (`-name`, `--name`,
        // `-name=value` — under the cursor OR just completed with a trailing
        // space) must NOT be joined into a greedy span: the completion
        // target is flag-land, and the tree suggester's flag paths (name
        // filtering, value completion, inline assignment) only fire when
        // the input is tokenized the way the core autocompleter tokenizes.
        boolean flagShapedTail = lastTokenIsFlagShaped(input);
        if (!flagShapedTail && parameter != null && builder != null && (parameter.isGreedy() || parameter.type().isGreedy(parameter))) {
            String originalRawInput = builder.getInput();
            String normalizedOriginal = normalizeInput(originalRawInput);
            int leadingOffset = originalRawInput.length() - normalizedOriginal.length();
            int greedyStartInInput = builder.getStart() - leadingOffset;
            if (greedyStartInInput >= argumentsStart && greedyStartInInput <= input.length()) {
                String preceding = input.substring(argumentsStart, greedyStartInInput);
                args = ArgumentInput.parse(preceding);
                String greedyValue = input.substring(greedyStartInInput);
                args.add(greedyValue);
            } else {
                int argumentsEnd = endsWithSpace ? input.length() - 1 : input.length();
                String argumentsSection = argumentsStart >= argumentsEnd
                                                  ? ""
                                                  : input.substring(argumentsStart, argumentsEnd);
                args = ArgumentInput.parseAutoCompletion(argumentsSection, endsWithSpace);
            }
        } else {
            int argumentsEnd = endsWithSpace ? input.length() - 1 : input.length();
            String argumentsSection = argumentsStart >= argumentsEnd
                                              ? ""
                                              : input.substring(argumentsStart, argumentsEnd);
            args = ArgumentInput.parseAutoCompletion(argumentsSection, endsWithSpace);
        }

        return dispatcher.config().getContextFactory().createSuggestionContext(dispatcher, source, command, label, args);
    }

    /**
     * True when the last whitespace-delimited token of {@code input} (the
     * token under the cursor — so only when the input does NOT end with a
     * space) is an inline flag assignment.
     */
    private boolean lastTokenIsFlagShaped(String input) {
        int end = input.length();
        while (end > 0 && Character.isWhitespace(input.charAt(end - 1))) {
            end--;
        }
        if (end == 0) {
            return false;
        }
        // With a trailing space the cursor targets a NEW token and the check
        // applies to the just-completed one: a flag right before the cursor
        // means the next position is a flag value (or, for a switch, more
        // flags) — not greedy text. Without a trailing space this inspects
        // the partial token under the cursor itself.
        int start = end;
        while (start > 0 && !Character.isWhitespace(input.charAt(start - 1))) {
            start--;
        }
        return studio.mevera.imperat.util.Patterns.isInputFlag(input.substring(start, end));
    }

    /**
     * Realigns {@code builder} so every emitted suggestion range is safe
     * for the client to apply against the visible text.
     *
     * <p>Two cases must be handled differently because the client
     * appends a trailing space for the empty-arg completion slot:</p>
     * <ul>
     * <li><b>Empty argument:</b> the insertion point must sit right
     * after the trailing space ({@code rawInput.length()}). Placing
     * it at the stripped length would glue the completion to the
     * command word ({@code /msgMyPlayerName}).</li>
     * <li><b>Non-empty partial token:</b> the range must replace
     * exactly the partial token and ignore any trailing space the
     * client appended, so {@code -sc} becomes {@code -sc castle}
     * and not {@code -sccastle }.</li>
     * </ul>
     * In both cases {@code start <= builder.getInput().length()} holds,
     * so no inverted ({@code start > end}) range is ever produced —
     * that is what caused the original
     * {@code StringIndexOutOfBoundsException} (e.g. inverted substring
     * range {@code Range [24, 19)}).</ul>
     */
    private SuggestionsBuilder alignToResolvedStart(
            SuggestionsBuilder builder,
            String rawInput,
            CompletionArg arg
    ) {
        String visibleInput = stripTrailingWhitespace(rawInput);
        if (arg.isEmpty()) {
            // Insert right after the trailing space the client sent/typed.
            return new SuggestionsBuilder(rawInput, rawInput.length());
        }
        // Replace exactly the partial token, ignoring any trailing space.
        int start = Math.max(0, visibleInput.length() - arg.value().length());
        return new SuggestionsBuilder(visibleInput, start);
    }

    private String stripTrailingWhitespace(String input) {
        int end = input.length();
        while (end > 0 && Character.isWhitespace(input.charAt(end - 1))) {
            end--;
        }
        return input.substring(0, end);
    }

    private String normalizeInput(String input) {
        while (input.startsWith("/")) {
            input = input.substring(1);
        }
        return input;
    }

    private void executor(ArgumentBuilder<?, ?> builder) {
        builder.executes((context) -> {
            String input = context.getInput();
            S sender = this.wrapCommandSource(context.getSource());
            dispatcher.execute(sender, input);
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        });
    }


    /**
     * Argument type for the per-scope {@code <flag>} node — consumes any
     * flag-shaped token (bare {@code -name}/{@code --name} or inline
     * {@code -name=value}). Default is the platform-agnostic
     * {@link FlagTokenArgumentType}. Backends whose registrar rejects raw
     * Brigadier types (e.g. modern Paper's {@code Commands} API requires
     * {@code CustomArgumentType} wrappers) MUST override to return a wrapped
     * instance — or {@code null} to skip flag nodes entirely (flags then
     * render red client-side but execution and server-side completions still
     * work).
     */
    protected @Nullable com.mojang.brigadier.arguments.ArgumentType<?> flagTokenArgumentType() {
        return new FlagTokenArgumentType();
    }

    /**
     * Argument type for the shared {@code <flag_value>} node that follows
     * the {@code <flag>} node. Generic across all of a scope's flags (the
     * node is built once per scope), so the default is a
     * {@link PermissiveStringArgumentType} — any single token. Value
     * completions come from the Imperat tree suggester, which resolves the
     * actual flag from the input. Backends needing wrapped types (modern
     * Paper) should override accordingly.
     */
    protected com.mojang.brigadier.arguments.@NotNull ArgumentType<?> flagValueArgumentType() {
        return new PermissiveStringArgumentType();
    }

    /**
     * Resolves the Brigadier {@link com.mojang.brigadier.arguments.ArgumentType}
     * for a positional string {@link Argument}. Greedy parameters use
     * {@link StringArgumentType#greedyString()} (consumes all remaining
     * input). Non-greedy parameters use {@link PermissiveStringArgumentType},
     * which accepts a single whitespace-delimited token with no character
     * restrictions, unlike Brigadier's stock {@code string()} which rejects
     * characters outside {@code [0-9A-Za-z._-+]}.
     */
    protected com.mojang.brigadier.arguments.ArgumentType<String> getStringArgType(Argument<S> parameter) {
        if (parameter.isGreedy()) {
            return StringArgumentType.greedyString();
        } else {
            return new PermissiveStringArgumentType();
        }
    }

    private @Nullable Argument<S> findActiveGreedyArgument(
            Command<S> command,
            String rawInput,
            @Nullable SuggestionsBuilder builder
    ) {
        if (builder == null) {
            return null;
        }
        String input = normalizeInput(rawInput);
        ActiveScope<S> scope = resolveActiveScope(command, input);
        Command<S> activeCommand = scope.command();
        int activeArgsStart = scope.argsStart();

        // Locate the greedy argument and count the POSITIONAL arguments that
        // precede it — command literals and flags are excluded because the
        // typed-token count below also excludes them. (The previous raw-index
        // count included the leading subcommand literal, so the comparison
        // could never match for subcommand-scoped greedy args.)
        Argument<S> greedyArg = null;
        int precedingExpected = 0;
        for (CommandPathway<S> pathway : activeCommand.getDedicatedPathways()) {
            int positionals = 0;
            for (Argument<S> argument : pathway.getArguments()) {
                if (argument.isCommand() || argument.isFlag()) {
                    continue;
                }
                if (argument.isGreedy() || argument.type().isGreedy(argument)) {
                    greedyArg = argument;
                    precedingExpected = positionals;
                    break;
                }
                positionals++;
            }
            if (greedyArg != null) {
                break;
            }
        }
        if (greedyArg == null) {
            return null;
        }

        String normalizedOriginal = normalizeInput(builder.getInput());
        int leadingOffset = builder.getInput().length() - normalizedOriginal.length();
        int nodeStartInInput = builder.getStart() - leadingOffset;

        if (nodeStartInInput < activeArgsStart) {
            return null;
        }

        boolean precedingIsValueFlag = false;
        int lastPos = nodeStartInInput - 1;
        while (lastPos >= activeArgsStart && Character.isWhitespace(input.charAt(lastPos))) {
            lastPos--;
        }
        if (lastPos >= activeArgsStart) {
            int tokenStart = lastPos;
            while (tokenStart > activeArgsStart && !Character.isWhitespace(input.charAt(tokenStart - 1))) {
                tokenStart--;
            }
            String precedingToken = input.substring(tokenStart, lastPos + 1);
            if (precedingToken.startsWith("-") && isValueFlag(activeCommand, precedingToken)) {
                precedingIsValueFlag = true;
            }
        }
        if (precedingIsValueFlag) {
            return null;
        }

        String precedingSection = input.substring(activeArgsStart, Math.min(nodeStartInInput, input.length()));
        ArgumentInput precedingArgs = ArgumentInput.parse(precedingSection);
        int actualPositionalCount = 0;
        for (int i = 0; i < precedingArgs.size(); i++) {
            String arg = precedingArgs.get(i);
            if (arg != null && arg.startsWith("-")) {
                if (isValueFlag(activeCommand, arg)) {
                    i++; // Skip the flag's value
                }
            } else {
                actualPositionalCount++;
            }
        }
        return actualPositionalCount >= precedingExpected ? greedyArg : null;
    }

    /**
     * Walks whitespace tokens of {@code input} (already {@link #normalizeInput
     * normalized}) against the command tree's literal children — aliases
     * included via {@link Command#hasName} — returning the deepest command
     * reached plus the char offset where its arguments begin.
     *
     * <p>Token-position based, replacing the previous
     * {@code String#indexOf(name)} arithmetic which broke in two ways: a
     * subcommand reached via an ALIAS had the primary name's length added at
     * the alias's index (offset landing mid-token), and a name occurring as a
     * substring of an earlier token anchored the offset to the wrong place.
     * Walking tree literals (instead of a {@code getSubCommand} name lookup)
     * also keeps the descent position-aware: a positional token stops the
     * walk rather than matching a same-named subcommand attached elsewhere
     * in the command.</p>
     */
    private ActiveScope<S> resolveActiveScope(Command<S> rootCommand, String input) {
        int length = input.length();
        int pos = 0;
        while (pos < length && Character.isWhitespace(input.charAt(pos))) {
            pos++;
        }
        int labelStart = pos;
        while (pos < length && !Character.isWhitespace(input.charAt(pos))) {
            pos++;
        }
        if (!rootCommand.hasName(input.substring(labelStart, pos))) {
            // Input doesn't lead with the command label — treat every token
            // as an argument of the root scope.
            return new ActiveScope<>(rootCommand, labelStart);
        }

        Node<S> node = rootCommand.tree().rootNode();
        Command<S> active = rootCommand;
        while (pos < length && Character.isWhitespace(input.charAt(pos))) {
            pos++;
        }
        int argsStart = pos;

        while (pos < length) {
            int tokenStart = pos;
            while (pos < length && !Character.isWhitespace(input.charAt(pos))) {
                pos++;
            }
            String token = input.substring(tokenStart, pos);

            Node<S> matched = null;
            for (Node<S> child : node.getChildren()) {
                Argument<S> main = child.getMainArgument();
                if (main.isCommand() && main.asCommand().hasName(token)) {
                    matched = child;
                    break;
                }
            }
            if (matched == null) {
                break;
            }
            node = matched;
            active = matched.getMainArgument().asCommand();
            while (pos < length && Character.isWhitespace(input.charAt(pos))) {
                pos++;
            }
            argsStart = pos;
        }
        return new ActiveScope<>(active, argsStart);
    }

    private record ActiveScope<S extends CommandSource>(Command<S> command, int argsStart) {
    }

    private boolean isValueFlag(Command<S> activeCommand, String token) {
        String name = token;
        while (name.startsWith("-")) {
            name = name.substring(1);
        }
        final String finalName = name;
        // Default pathway included: a custom global-default pathway may carry
        // flags of its own (mirrors the core suggester's effectivePathways).
        List<CommandPathway<S>> pathways = new java.util.ArrayList<>(activeCommand.getDedicatedPathways());
        pathways.add(activeCommand.getDefaultPathway());
        for (CommandPathway<S> pathway : pathways) {
            for (FlagArgument<S> flag : pathway.getFlagExtractor().getRegisteredFlags()) {
                if (flag.getName().equalsIgnoreCase(finalName) || flag.flagData().aliases().stream().anyMatch(alias -> alias.equalsIgnoreCase(finalName))) {
                    return !flag.isSwitch();
                }
            }
        }
        return false;
    }

}
