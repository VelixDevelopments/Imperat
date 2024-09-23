package dev.velix.imperat.command.tree;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.suggestions.AutoCompleteList;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.SuggestionContext;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.util.TypeUtility;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Mqzen
 */
@Getter
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
        for (CommandUsage<S> usage : root.data.usages()) {
            parseUsage(usage);
        }
    }
    
    public void parseUsage(CommandUsage<S> usage) {
        List<CommandParameter<S>> parameters = usage.getParameters();
        if (parameters == null || parameters.isEmpty()) {
            return;
        }
        addParametersToTree(root, parameters, 0);
    }
    
    private void addParametersToTree(
            ParameterNode<S, ?> currentNode,
            List<CommandParameter<S>> parameters,
            int index
    ) {
        if (index >= parameters.size()) {
            return;
        }
        
        CommandParameter<S> param = parameters.get(index);
        ParameterNode<S,?> childNode = getChildNode(currentNode, param);
        // Recursively add the remaining parameters to the child node
        addParametersToTree(childNode, parameters, index + 1);
    }
    
    private ParameterNode<S, ?> getChildNode(ParameterNode<S, ?> parent, CommandParameter<S> param) {
        for (ParameterNode<S, ?> child : parent.getChildren()) {
            if (child.data.name().equalsIgnoreCase(param.name())
                    && TypeUtility.matches(child.data.type(), param.type())) {
                return child;
            }
        }
        ParameterNode<S, ?> newNode;
        if (param.isCommand())
            newNode = new CommandNode<>(param.asCommand());
        else
            newNode = new ArgumentNode<>(param);
        
        parent.addChild(newNode);
        return newNode;
    }
    
    public @NotNull List<String> tabComplete(Imperat<S> imperat, SuggestionContext<S> context) {
        final int depthToReach = context.getArgToComplete().index();
        
        AutoCompleteList results = new AutoCompleteList();
        for (var child : root.getChildren()) {
            results.addAll(
                    collectNodeCompletions(imperat, context, child, 0, depthToReach, results)
            );
        }
        return results.asList();
    }
    
    private AutoCompleteList collectNodeCompletions(
            Imperat<S> imperat,
            SuggestionContext<S> context,
            ParameterNode<S, ?> child,
            int depth,
            final int maxDepth,
            AutoCompleteList results
    ) {
        if (depth > maxDepth) {
            return results;
        }
        
        String raw = context.arguments().getOr(depth, "");
        assert raw != null;
        
        if ((!raw.isBlank() || !raw.isEmpty()) && !child.matchesInput(raw)
                || (!root.data.isIgnoringACPerms() && !imperat.getPermissionResolver()
                .hasPermission(context.source(), child.data.permission()))) {
            return results;
        }
        
        if (depth == maxDepth) {
            //we reached the arg we want to complete, let's complete it using our current node
            //COMPLETE DIRECTLY
            addChildResults(imperat, context, child, results);
            return results;
        } else {
            //Keep looking
            for (var innerChild : child.getChildren()) {
                results.addAll(
                        collectNodeCompletions(imperat, context, innerChild, depth + 1, maxDepth, results)
                );
            }
        }
        return results;
    }
    
    private void addChildResults(
            Imperat<S> imperat,
            SuggestionContext<S> context,
            ParameterNode<S, ?> node,
            AutoCompleteList results
    ) {
        if (node instanceof CommandNode<?>) {
            results.add(node.data.name());
            results.addAll(node.data.asCommand().aliases());
        } else {
            SuggestionResolver<S, ?> resolver = imperat.getParameterSuggestionResolver(node.data);
            if (resolver == null) return;
            List<String> autoCompletions = resolver.autoComplete(context, node.data);
            results.addAll(autoCompletions);
        }
    }
    
    
    //context matching part
    public @NotNull UsageContextMatch<S> contextMatch(
            ArgumentQueue input
    ) {
        
        int depth = 0;
        
        for (ParameterNode<S, ?> child : root.getChildren()) {
            UsageContextMatch<S> nodeTraversing = UsageContextMatch.of();
            var traverse = contextMatchNode(nodeTraversing, input, child, depth);
            if (traverse.result() != UsageMatchResult.UNKNOWN) {
                return traverse;
            }
            
        }
        
        return UsageContextMatch.of();
    }
    
    private @NotNull UsageContextMatch<S> contextMatchNode(
            UsageContextMatch<S> usageContextMatch,
            ArgumentQueue input,
            ParameterNode<S, ?> currentNode,
            int depth
    ) {
        //ImperatDebugger.debug("Traversing node=%s, at depth=%s", node.format(), depth);
        if (depth >= input.size()) {
            return usageContextMatch;
        }
        
        String raw = input.get(depth);
        boolean matchesInput = currentNode.matchesInput(raw);
        if (!matchesInput) {
            return usageContextMatch;
        }
        //GO TO NODE'S children
        usageContextMatch.append(currentNode);
        if (currentNode.isLeaf()) {
            
            //not the deepest search depth (still more raw args than number of nodes)
            usageContextMatch.setResult((depth != input.size() - 1 && !currentNode.isGreedyParam())
                    ? UsageMatchResult.INCOMPLETE : UsageMatchResult.COMPLETE);
            return usageContextMatch;
        } else {
            //not the last node → continue traversing
            
            //checking if depth is the last
            if (depth == input.size() - 1) {
                //depth is last → check for missing required arguments
                usageContextMatch.append(currentNode);
                
                if (currentNode.isOptional()) {
                    //so if the node is optional,
                    // we go deeper into the tree, while backtracking the depth of the argument input.
                    return searchForMatch(currentNode, usageContextMatch, input, depth - 1);
                } else {
                    //ImperatDebugger.debug("Last Depth=%s, Current node= %s", depth, node.format());
                    //node is not the last, and we reached the end of the raw input length
                    //We check if there's any missing optional
                    boolean allOptional = true;
                    for (ParameterNode<S, ?> child : currentNode.getChildren()) {
                        if (!child.isOptional()) {
                            allOptional = false;
                            break;
                        }
                    }
                    
                    //improved logic
                    if (allOptional) {
                        //if all optional, then append the all next
                        var child = currentNode.getChild(ParameterNode::isOptional);
                        if (child != null) {
                            usageContextMatch.append(child);
                            //collect optionals while depth is constant since we reached the end of raw input early
                            if (!child.isLeaf()) {
                                return searchForMatch(child, usageContextMatch, input, depth - 1);
                            }
                        }
                    }
                    
                    //ImperatDebugger.debug("All optional after last depth ? = %s", (allOptional) );
                    var usage = usageContextMatch.toUsage(root.data);
                    usageContextMatch.setResult(
                            allOptional || usage != null
                                    ? UsageMatchResult.COMPLETE
                                    : UsageMatchResult.INCOMPLETE
                    );
                    return usageContextMatch;
                }
                
            } else {
                return this.searchForMatch(currentNode, usageContextMatch, input, depth);
            }
            
        }
        
    }
    
    private UsageContextMatch<S> searchForMatch(
            ParameterNode<S, ?> node,
            UsageContextMatch<S> usageContextMatch,
            ArgumentQueue input,
            int depth
    ) {
        for (ParameterNode<S, ?> child : node.getChildren()) {
            var traversedChild = contextMatchNode(usageContextMatch, input, child, depth + 1);
            if (traversedChild.result() == UsageMatchResult.COMPLETE)
                return traversedChild;
        }
        return usageContextMatch;
    }
    
}
