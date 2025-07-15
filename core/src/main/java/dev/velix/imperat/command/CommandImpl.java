package dev.velix.imperat.command;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.FlagParameter;
import dev.velix.imperat.command.processors.CommandPostProcessor;
import dev.velix.imperat.command.processors.CommandPreProcessor;
import dev.velix.imperat.command.processors.CommandProcessingChain;
import dev.velix.imperat.command.suggestions.AutoCompleter;
import dev.velix.imperat.command.tree.CommandDispatch;
import dev.velix.imperat.command.tree.CommandTree;
import dev.velix.imperat.command.tree.CommandTreeVisualizer;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.FlagData;
import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.help.HelpProvider;
import dev.velix.imperat.help.PaginatedHelpTemplate;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.util.ImperatDebugger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ApiStatus.Internal
final class CommandImpl<S extends Source> implements Command<S> {

    private final String name;
    private final int position;
    private final List<String> aliases = new ArrayList<>();
    private final Map<String, Command<S>> children = new LinkedHashMap<>();
    private final UsageMap<S> usages = new UsageMap<>();
    private final AutoCompleter<S> autoCompleter;
    private final @Nullable CommandTree<S> commandTree;
    private final @NotNull CommandTreeVisualizer<S> visualizer;
    private String permission = null;
    private Description description = Description.EMPTY;
    private boolean suppressACPermissionChecks = false;
    private CommandUsage<S> mainUsage = null;
    private CommandUsage<S> defaultUsage;

    private final @NotNull CommandProcessingChain<S, CommandPreProcessor<S>> preProcessors =
            CommandProcessingChain.<S>preProcessors()
            .build();

    private final @NotNull CommandProcessingChain<S, CommandPostProcessor<S>> postProcessors =
            CommandProcessingChain.<S>postProcessors()
            .build();

    private @Nullable HelpProvider<S> helpProvider;
    private @Nullable Command<S> parent;
    private final CommandUsage<S> emptyUsage;
    private final @NotNull SuggestionResolver<S> suggestionResolver;
    private final @NotNull Set<FlagData<S>> freeFlags = new HashSet<>();

    CommandImpl(String name) {
        this(null, name);
    }

    //sub-command constructor
    CommandImpl(@Nullable Command<S> parent, String name) {
        this(parent, 0, name);
    }

    CommandImpl(@Nullable Command<S> parent, int position, String name) {
        this.parent = parent;
        this.position = position;
        this.name = name.toLowerCase();
        this.emptyUsage = CommandUsage.<S>builder().build(this);
        this.defaultUsage = CommandUsage.<S>builder().build(this);
        
        this.autoCompleter = AutoCompleter.createNative(this);
        this.commandTree = parent != null ? null : CommandTree.create(this);
        this.visualizer = CommandTreeVisualizer.of(commandTree);
        this.suggestionResolver = SuggestionResolver.forCommand(this);

    }

    /**
     * @return the name of the command
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * @return The permission of the command
     */
    @Override
    public @Nullable String permission() {
        return permission;
    }

    /**
     * Sets the permission of a command
     *
     * @param permission the permission of a command
     */
    @Override
    public void permission(@Nullable String permission) {
        this.permission = permission;
    }

    /**
     * @return The description of a command
     */
    @Override
    public @NotNull Description description() {
        return description;
    }

    @Override
    public void describe(Description description) {
        this.description = description;
    }

    /**
     * @return the index of this parameter
     */
    @Override
    public int position() {
        return position;
    }

    /**
     * Sets the position of this command in a syntax
     * DO NOT USE THIS FOR ANY REASON unless it's necessary to do so
     *
     * @param position the position to set
     */
    @Override
    public void position(int position) {
        throw new UnsupportedOperationException("You can't modify the position of a command");
    }

    /**
     * Retrieves the HelpProvider instance associated with the current context.
     *
     * @return the HelpProvider instance of type S
     */
    @Override
    public @Nullable HelpProvider<S> getHelpProvider() {
        return helpProvider;
    }

    /**
     * Sets the help provider for the current context. The provided help provider can be used
     * to supply contextual help or assistance in various scenarios.
     *
     * @param helpProvider the help provider instance to set. Can be null to indicate
     *                     that no help provider is to be used.
     */
    @Override
    public void setHelpProvider(@Nullable HelpProvider<S> helpProvider) {
        this.helpProvider = helpProvider;
    }

    @Override
    public @NotNull CommandDispatch<S> contextMatch(Context<S> context) {
        if (commandTree != null) {
            var copy = context.arguments().copy();
            copy.removeIf(String::isBlank);
            return commandTree.contextMatch(copy, context.imperat().config());
        } else {
            throw new IllegalCallerException("Cannot match a sub command in a root's execution !");
        }
    }

    @Override
    public void visualizeTree() {
        ImperatDebugger.debug("Visualizing %s's tree", this.name);
        visualizer.visualize();
    }

    /**
     * Sets a pre-processor for the command
     *
     * @param preProcessor the pre-processor for the command
     */
    @Override
    public void addPreProcessor(@NotNull CommandPreProcessor<S> preProcessor) {
        this.preProcessors.add(preProcessor);
    }

    /**
     * Executes the pre-processing instructions in {@link CommandPreProcessor}
     *
     * @param api     the api
     * @param context the context
     * @param usage   the usage detected being used
     */
    @Override
    public boolean preProcess(@NotNull Imperat<S> api, @NotNull Context<S> context, @NotNull CommandUsage<S> usage) {
        for(var processor : preProcessors) {
            try {
                processor.process(api, context, usage);
            } catch (ImperatException e) {
                api.config().handleExecutionThrowable(e, context, processor.getClass(), "Command#preProcess");
                return false;
            }
        }

        return true;
    }

    /**
     * Sets a post-processor for the command
     *
     * @param postProcessor the post-processor for the command
     */
    @Override
    public void addPostProcessor(@NotNull CommandPostProcessor<S> postProcessor) {
        this.postProcessors.add(postProcessor);
    }

    /**
     * Executes the post-processing instructions in {@link CommandPostProcessor}
     *
     * @param api     the api
     * @param context the context
     * @param usage   the usage detected being used
     */
    @Override
    public boolean postProcess(@NotNull Imperat<S> api, @NotNull ResolvedContext<S> context, @NotNull CommandUsage<S> usage) {
        for(var processor : postProcessors) {
            try {
                processor.process(api, context);
            } catch (ImperatException e) {
                api.config().handleExecutionThrowable(e, context, processor.getClass(), "Command#postProcess");
                return false;
            }

        }
        return true;
    }

    /**
     * Retrieves a usage with no args for this command
     *
     * @return A usage with empty parameters.
     */
    @Override 
    public @NotNull CommandUsage<S> getEmptyUsage() {
        return emptyUsage;
    }

    /**
     * Casts the parameter to a flag parameter
     *
     * @return the parameter as a flag
     */
    @Override
    public FlagParameter<S> asFlagParameter() {
        throw new UnsupportedOperationException("A command cannot be treated as a flag !");
    }

    @Override
    public boolean isGreedyString() {
        return false;
    }

    /**
     * Fetches the suggestion resolver linked to this
     * command parameter.
     *
     * @return the {@link SuggestionResolver} for a resolving suggestion
     */
    @Override
    public @NotNull SuggestionResolver<S> getSuggestionResolver() {
        return suggestionResolver;
    }

    @Override
    public boolean similarTo(CommandParameter<?> parameter) {
        return this.name.equalsIgnoreCase(parameter.name());
    }

    /**
     * @return the aliases for this commands
     */
    @Override
    public @UnmodifiableView List<String> aliases() {
        return aliases;
    }

    @Override
    public CommandTree<S> tree() {
        return this.commandTree;
    }

    /**
     * Sets the aliases of a command
     *
     * @param aliases the aliases for te command to set
     */
    @Override
    public void addAliases(List<String> aliases) {
        for (String alias : aliases)
            this.aliases.add(alias.toLowerCase());
    }

    /**
     * @return the default usage of the command
     * without any args
     */
    @Override
    public @NotNull CommandUsage<S> getDefaultUsage() {
        return defaultUsage;
    }

    /**
     * Sets the default command usage representation.
     *
     * @param usage the default command usage instance to be set, which must not be null
     */
    @Override
    public void setDefaultUsage(@NotNull CommandUsage<S> usage) {
        this.defaultUsage = Objects.requireNonNull(usage, "Default usage cannot be null");
    }
    

    /**
     * Adds a usage to the command
     *
     * @param usage the usage {@link CommandUsage} of the command
     */
    @Override
    public void addUsage(CommandUsage<S> usage) {
        if (usage.isDefault()) {
            this.defaultUsage = usage;
        }

        usages.put(usage.getParameters(), usage);

        if (mainUsage == null && !usage.isDefault() && usage.getMaxLength() >= 1 && !usage.hasParamType(Command.class)) {
            mainUsage = usage;
        }

        if (commandTree != null) commandTree.parseUsage(usage);
    }

    @Override
    public @Nullable CommandUsage<S> getUsage(List<CommandParameter<S>> parameters) {
        if (parameters.isEmpty()) {
            //default
            return defaultUsage;
        }

        var mapped = usages.get(parameters);
        if(mapped == null) {
            for(var usage : usages()) {
                if(usage.hasParameters(parameters)) {
                    return usage;
                }
            }
        }


        return mapped;
    }

    /**
     * @return all {@link CommandUsage} that were registered
     * to this command by the user
     */
    @Override
    public Collection<? extends CommandUsage<S>> usages() {
        return usages.asSortedSet();
    }

    @Override
    public Collection<? extends CommandUsage<S>> findUsages(Predicate<CommandUsage<S>> predicate) {
        return usages.values().stream().filter(predicate)
            .collect(Collectors.toList());
    }

    /**
     * @return the usage that doesn't include any subcommands, only
     * required parameters
     */
    @Override
    public @NotNull CommandUsage<S> getMainUsage() {
        return Optional.ofNullable(mainUsage)
            .orElse(defaultUsage);
    }

    /**
     * @return Returns {@link AutoCompleter}
     * that handles all auto-completes for this command
     */
    @Override
    public AutoCompleter<S> autoCompleter() {
        return autoCompleter;
    }

    /**
     * @return Whether this command is a sub command or not
     */
    @Override
    public @Nullable Command<S> parent() {
        return parent;
    }

    /**
     * sets the parent command
     *
     * @param parent the parent to set.
     */
    @Override
    public void parent(@NotNull Command<S> parent) {
        this.parent = parent;
    }

    @Override
    public void registerSubCommand(Command<S> command) {
        children.put(command.name(), command);
    }

    /**
     * Injects a created-subcommand directly into the parent's command usages.
     *
     * @param command        the subcommand to inject
     * @param attachmentMode see {@link AttachmentMode}
     */
    @Override
    public void addSubCommand(Command<S> command, AttachmentMode attachmentMode) {
        command.parent(this);
        registerSubCommand(command);

        final CommandUsage<S> prime;
        switch (attachmentMode) {
            case EMPTY -> prime = getEmptyUsage();
            case MAIN, UNSET -> prime = getMainUsage();
            case DEFAULT -> prime = getDefaultUsage();
            default -> throw new IllegalArgumentException("Unknown attachment mode: " + attachmentMode);
        }
        CommandUsage<S> combo = prime.mergeWithCommand(command, command.getMainUsage());
        //adding the merged command usage

        //ImperatDebugger.debug("Trying to add usage `%s`", CommandUsage.format(this, combo));
        this.addUsage(combo);

        for (CommandUsage<S> subUsage : command.usages()) {
            if (subUsage.equals(command.getMainUsage())) continue;
            combo = prime.mergeWithCommand(command, subUsage);
            //adding the merged command usage

            //ImperatDebugger.debug("Trying to add usage `%s`", CommandUsage.format(this, combo));
            this.addUsage(
                combo
            );
        }

    }
    
    @Override
    public void addSubCommandUsage(
        String subCommand,
        List<String> aliases,
        CommandUsage.Builder<S> usage,
        AttachmentMode attachmentMode
    ) {
        int position;
        if (attachmentMode == AttachmentMode.EMPTY) {
            position = position() + 1;
        } else {
            CommandUsage<S> main = attachmentMode == AttachmentMode.MAIN ? getMainUsage() : getDefaultUsage();
            position = this.position() + (main.getMinLength() == 0 ? 1 : main.getMinLength());
        }

        //creating subcommand to modify
        Command<S> subCmd =
            Command.create(this, position, subCommand.toLowerCase())
                .aliases(aliases)
                .usage(usage)
                .build();
        addSubCommand(subCmd, attachmentMode);
    }

    /**
     * @param name the name of the wanted sub-command
     * @return the sub-command of a specific name directly from
     * this instance of a command, meaning that
     * it won't go deeply in search for sub-command
     */
    @Override
    public @Nullable Command<S> getSubCommand(String name) {
        Command<S> sub = children.get(name);
        if (sub != null)
            return sub;

        for (String subsNames : children.keySet()) {
            Command<S> other = children.get(subsNames);
            if (other.hasName(name)) return other;
            else if (subsNames.startsWith(name)) return other;
        }
        return null;
    }

    /**
     * @return the subcommands of this command
     */
    @Override
    public @NotNull Collection<? extends Command<S>> getSubCommands() {
        return children.values();
    }

    /**
     * whether to ignore permission checks on the auto-completion of command and
     * sub commands or not
     *
     * @return whether to ignore permission checks on the auto-completion of command and
     * sub commands or not
     */
    @Override
    public boolean isIgnoringACPerms() {
        return suppressACPermissionChecks;
    }

    /**
     * if true, it will ignore permission checks
     * on the auto-completion of command and sub commands
     * <p>
     * otherwise, it will perform permission checks and
     * only tab-completes the usages/subcommands that you have permission for
     *
     * @param suppress true if you want to ignore the permission checks on tab completion of args
     */
    @Override
    public void ignoreACPermissions(boolean suppress) {
        this.suppressACPermissionChecks = suppress;
    }

    /**
     * Adds help as a sub-command to the command chain
     *
     * @param dispatcher    the api
     * @param params        the parameters of the help command
     * @param helpExecution the help execution
     */
    @Override
    public void addHelpCommand(Imperat<S> dispatcher, List<CommandParameter<S>> params, CommandExecution<S> helpExecution) {
        if (params.isEmpty() && dispatcher.config().getHelpProvider() instanceof PaginatedHelpTemplate) {
            params.add(
                CommandParameter.<S>optionalInt("page")
                    .description("help-page")
                    .defaultValue(1)
                    .build()
            );
        }

        addSubCommandUsage(
            "help",
            CommandUsage.<S>builder()
                .parameters(params)
                .execute(helpExecution),
            AttachmentMode.MAIN
        );
    }

    @Override
    public void registerFlag(FlagData<S> flag) {
        freeFlags.add(flag);
    }

    @Override
    public Optional<FlagData<S>> getFlagFromRaw(String raw) {
        return freeFlags.stream()
            .filter((data) -> data.acceptsInput(raw)).findFirst();
    }

    @Override
    public Set<FlagData<S>> getRegisteredFlags() {
        return freeFlags;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommandImpl<?> command)) return false;
        return Objects.equals(name, command.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
