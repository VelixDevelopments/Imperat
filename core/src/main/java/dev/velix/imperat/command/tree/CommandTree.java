package dev.velix.imperat.command.tree;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.suggestions.CompletionArg;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.util.TypeUtility;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mqzen
 */
public final class CommandTree<S extends Source> {

    final CommandNode<S> root;

    CommandTree(Command<S> command) {
        this.root = new CommandNode<>(command);
        //parse(command);
    }

    public static <S extends Source> CommandTree<S> create(Command<S> command) {
        return new CommandTree<>(command);
    }

    public static <S extends Source> CommandTree<S> parsed(Command<S> command) {
        CommandTree<S> tree = create(command);
        tree.parseCommandUsages();
        return tree;
    }
    
    //parsing usages part
    public void parseCommandUsages() {
        for (CommandUsage<S> usage : root.data.getUsages()) {
            parseUsage(usage);
        }
    }

    public void parseUsage(CommandUsage<S> usage) {
        List<CommandParameter> parameters = usage.getParameters();
        if (parameters == null || parameters.isEmpty()) {
            return;
        }
        addParametersToTree(root, parameters, 0);
    }

    private void addParametersToTree(
            UsageNode<?> currentNode,
            List<CommandParameter> parameters,
            int index
    ) {
        if (index >= parameters.size()) {
            return;
        }

        CommandParameter param = parameters.get(index);
        UsageNode<?> childNode = getChildNode(currentNode, param);
        // Recursively add the remaining parameters to the child node
        addParametersToTree(childNode, parameters, index + 1);
    }

    private UsageNode<?> getChildNode(UsageNode<?> parent, CommandParameter param) {
        for (UsageNode<?> child : parent.getChildren()) {
            if (child.data.getName().equalsIgnoreCase(param.getName())
                    && TypeUtility.matches(child.data.getType(), param.getType())) {
                return child;
            }
        }
        UsageNode<?> newNode;
        if (param.isCommand())
            newNode = new CommandNode<>(param.asCommand());
        else
            newNode = new ArgumentNode(param);

        parent.addChild(newNode);
        return newNode;
    }
    
    public @NotNull List<String> tabComplete(Imperat<S> imperat, S source, CompletionArg arg, ArgumentQueue raws) {
        
        final int depthToReach = arg.index();
        
        List<String> results = new ArrayList<>();
        for (var child : root.getChildren()) {
            results.addAll(
                    collectNodeCompletions(imperat, source, raws,
                            arg, child, 0, depthToReach, results)
            );
        }
        return results;
    }
    
    private List<String> collectNodeCompletions(
            Imperat<S> imperat,
            S source,
            ArgumentQueue raws,
            CompletionArg arg,
            UsageNode<?> child,
            int depth,
            final int maxDepth,
            List<String> results
    ) {
        if (depth > maxDepth) {
            return results;
        }
        
        String raw = raws.getOr(depth, "");
        assert raw != null;
        
        if ((!raw.isBlank() || !raw.isEmpty()) && !child.matchesInput(raw)) {
            return results;
        }
        
        if (depth == maxDepth) {
            //we reached the arg we want to complete, let's complete it using our current node
            //COMPLETE DIRECTLY
            addChildResults(imperat, source, raws, arg, child, results);
            return results;
        } else {
            //Keep looking
            for (var innerChild : child.getChildren()) {
                results.addAll(
                        collectNodeCompletions(imperat, source, raws, arg, innerChild, depth + 1, maxDepth, results)
                );
            }
        }
        return results;
    }
    
    private void addChildResults(
            Imperat<S> imperat,
            S source,
            ArgumentQueue raws,
            CompletionArg arg,
            UsageNode<?> node,
            List<String> results
    ) {
        if (node instanceof CommandNode<?>) {
            results.add(node.data.getName());
            results.addAll(node.data.asCommand().getAliases());
        } else {
            SuggestionResolver<S, ?> resolver = imperat.getParameterSuggestionResolver(node.data);
            if (resolver == null) return;
            List<String> autoCompletions = resolver.autoComplete(root.data, source, raws, node.data, arg);
            results.addAll(autoCompletions);
        }
    }
    
    
    //context matching part
    public @NotNull UsageContextMatch contextMatch(
            ArgumentQueue input
    ) {

        int depth = 0;

        for (UsageNode<?> child : root.getChildren()) {
            UsageContextMatch nodeTraversing = UsageContextMatch.of();
            var traverse = contextMatchNode(nodeTraversing, input, child, depth);
            if (traverse.result() != UsageMatchResult.UNKNOWN) {
                return traverse;
            }

        }
        
        return UsageContextMatch.of();
    }
    
    private @NotNull UsageContextMatch contextMatchNode(
            UsageContextMatch usageContextMatch,
            ArgumentQueue input,
            UsageNode<?> node,
            int depth
    ) {
        //CommandDebugger.debug("Traversing node=%s, at depth=%s", node.format(), depth);
        if (depth >= input.size()) {
            return usageContextMatch;
        }

        String raw = input.get(depth);
        boolean matchesInput = node.matchesInput(raw);
        if (!matchesInput) {
            return usageContextMatch;
        }
        //GO TO NODE'S children
        usageContextMatch.append(node);
        if (node.isLeaf()) {

            //not the deepest search depth (still more raw args than number of nodes)
            usageContextMatch.setResult((depth != input.size() - 1 && !node.isGreedyParam())
                    ? UsageMatchResult.INCOMPLETE : UsageMatchResult.COMPLETE);
            return usageContextMatch;
        } else {
            //not the last node → continue traversing

            //checking if depth is the last
            if (depth == input.size() - 1) {
                //depth is last → check for missing required arguments
                usageContextMatch.append(node);

                if (node.isOptional()) {
                    //so if the node is optional,
                    // we go deeper into the tree, while backtracking the depth of the argument input.
                    return searchForMatch(node, usageContextMatch, input, depth - 1);
                } else {
                    //CommandDebugger.debug("Last Depth=%s, Current node= %s", depth, node.format());
                    //node is not the last, and we reached the end of the raw input length
                    //We check if there's any missing optional
                    boolean allOptional = true;
                    for (UsageNode<?> child : node.getChildren()) {
                        if (!child.isOptional()) {
                            allOptional = false;
                            break;
                        }
                    }

                    //improved logic
                    if (allOptional) {
                        //if all optional, then append the first one
                        usageContextMatch.append(node.getChild(UsageNode::isOptional));
                    }

                    //CommandDebugger.debug("All optional after last depth ? = %s", (allOptional) );
                    var usage = usageContextMatch.toUsage(root.data);
                    usageContextMatch.setResult(
                            allOptional || usage != null
                                    ? UsageMatchResult.COMPLETE
                                    : UsageMatchResult.INCOMPLETE
                    );
                    return usageContextMatch;
                }

            } else {
                return this.searchForMatch(node, usageContextMatch, input, depth);
            }

        }

    }
    
    private UsageContextMatch searchForMatch(
            UsageNode<?> node,
            UsageContextMatch usageContextMatch,
            ArgumentQueue input,
            int depth
    ) {
        for (UsageNode<?> child : node.getChildren()) {
            var traversedChild = contextMatchNode(usageContextMatch, input, child, depth + 1);
            if (traversedChild.result() == UsageMatchResult.COMPLETE)
                return traversedChild;
        }
        return usageContextMatch;
    }

}
