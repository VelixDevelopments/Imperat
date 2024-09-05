package dev.velix.imperat.command.tree;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.NotNull;

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
            if (child.data.equals(param)) {
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

    public @NotNull Traverse traverse(
            ArgumentQueue input
    ) {

        int depth = 0;

        for (UsageNode<?> child : root.getChildren()) {
            Traverse nodeTraversing = Traverse.of();
            var traverse = traverseNode(nodeTraversing, input, child, depth);
            if (traverse.result() != TraverseResult.UNKNOWN) {
                return traverse;
            }

        }

        return Traverse.of();
    }

    private Traverse traverseNode(
            Traverse traverse,
            ArgumentQueue input,
            UsageNode<?> node,
            int depth
    ) {
        //CommandDebugger.debug("Traversing node=%s, at depth=%s", node.format(), depth);
        if (depth >= input.size()) {
            return traverse;
        }

        String raw = input.get(depth);
        boolean matchesInput = node.matchesInput(raw);
        if (!matchesInput) {
            return traverse;
        }
        //GO TO NODE'S children
        traverse.append(node);
        if (node.isLeaf()) {

            //not the deepest search depth (still more raw args than number of nodes)
            traverse.setResult((depth != input.size() - 1 && !node.isGreedyParam())
                    ? TraverseResult.INCOMPLETE : TraverseResult.COMPLETE);
            return traverse;
        } else {
            //not the last node → continue traversing

            //checking if depth is the last
            if (depth == input.size() - 1) {
                //depth is last → check for missing required arguments
                traverse.append(node);

                if (node.isOptional()) {
                    //so if the node is optional,
                    // we go deeper into the tree, while backtracking the depth of the argument input.
                    return goDeeper(node, traverse, input, depth - 1);
                } else {
                    //CommandDebugger.debug("Last Depth=%s, Current node= %s", depth, node.format());
                    //node is not the last, and we reached the end of the raw input length
                    //We check if there's any missing optional
                    boolean allOptional = true;
                    for (UsageNode<?> child : node.getChildren()) {
                        if (!child.isOptional()) {
                            allOptional = false;
                            break;
                        } else {
                            traverse.append(child);
                        }
                    }
                    //CommandDebugger.debug("All optional after last depth ? = %s", (allOptional) );
                    traverse.setResult(
                            allOptional ? TraverseResult.COMPLETE : TraverseResult.INCOMPLETE
                    );
                    return traverse;
                }

            } else {
                return this.goDeeper(node, traverse, input, depth);
            }

        }

    }

    private Traverse goDeeper(
            UsageNode<?> node,
            Traverse traverse,
            ArgumentQueue input,
            int depth
    ) {
        for (UsageNode<?> child : node.getChildren()) {
            var traversedChild = traverseNode(traverse, input, child, depth + 1);
            if (traversedChild.result() == TraverseResult.COMPLETE)
                return traversedChild;
        }
        return traverse;
    }

}
