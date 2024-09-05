package dev.velix.imperat;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.tree.CommandTree;
import dev.velix.imperat.command.tree.CommandTreeVisualizer;
import dev.velix.imperat.command.tree.Traverse;
import dev.velix.imperat.command.tree.TraverseResult;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.util.CommandDebugger;
import dev.velix.imperat.util.TypeWrap;
import dev.velix.imperat.verification.UsageVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TestRun {
    
    TestRun() {
    }
    
    private static Traverse testCmdTreeExecution(Command<TestSender> cmd, String input) {
        CommandTree<TestSender> tree = CommandTree.create(cmd);
        tree.parseCommandUsages();
        
        CommandDebugger.debug("Visualizing tree");
        CommandTreeVisualizer<TestSender> visualizer = CommandTreeVisualizer.of(tree);
        visualizer.visualize();
        
        // command= /test
        ArgumentQueue argumentQueue = ArgumentQueue.parse(input);
        CommandDebugger.debug("traversing...");
        
        CommandDebugger.debug("Visualizing traversing result");
        Traverse traverse = tree.traverse(argumentQueue);
        traverse.visualize();
        
        return traverse;
    }
    
    @Test
    public void testTypeWrap() {
        final TypeWrap<List<String>> typeWrap = new TypeWrap<>() {
        };
        Assertions.assertEquals("java.util.List<java.lang.String>", typeWrap.getType().getTypeName());
    }
    
    @Test
    public void testTypeTolerantVerifierAmbiguity() {
        UsageVerifier<TestSender> verifier = UsageVerifier.typeTolerantVerifier();
        
        CommandUsage<TestSender> usage1 = CommandUsage.<TestSender>builder()
                .parameters(
                        CommandParameter.requiredText("arg1")
                ).build();
        
        CommandUsage<TestSender> usage2 = CommandUsage.<TestSender>builder()
                .parameters(
                        CommandParameter.requiredBoolean("arg2")
                ).build();
        
        Assertions.assertFalse(verifier.areAmbiguous(usage1, usage2));
    }
    
    @Test
    public void testIncompleteSubCommand() {
        Traverse traverse = testCmdTreeExecution(TestCommands.GROUP_CMD, "member setperm");
        Assertions.assertEquals(TraverseResult.INCOMPLETE, traverse.result());
    }
}
