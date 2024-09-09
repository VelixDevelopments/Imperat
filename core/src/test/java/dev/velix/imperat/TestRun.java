package dev.velix.imperat;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.tree.UsageMatchResult;
import dev.velix.imperat.commands.annotations.TestCommand;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.processors.CustomPostProcessor;
import dev.velix.imperat.processors.CustomPreProcessor;
import dev.velix.imperat.util.TypeWrap;
import dev.velix.imperat.verification.UsageVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static dev.velix.imperat.commands.TestCommands.GROUP_CMD;
import static dev.velix.imperat.commands.TestCommands.MULTIPLE_OPTIONAL_CMD;

public class TestRun {

    TestRun() {
        USAGE_EXECUTED = false;
    }

    private final static TestImperat IMPERAT = new TestImperat();
    private final static TestSource SOURCE = new TestSource(System.out);

    public static volatile boolean USAGE_EXECUTED = false;
    public static volatile int POST_PROCESSOR_INT = 0;
    public static volatile int PRE_PROCESSOR_INT = 0;

    static {
        IMPERAT.setUsageVerifier(UsageVerifier.typeTolerantVerifier());

        IMPERAT.registerCommand(GROUP_CMD);
        IMPERAT.registerCommand(MULTIPLE_OPTIONAL_CMD);
        //IMPERAT.registerCommand(CHAINED_SUBCOMMANDS_CMD);
        IMPERAT.registerCommand(new TestCommand());
    }
    
    private static UsageMatchResult testCmdTreeExecution(String cmdName, String input) {
        return IMPERAT.dispatch(SOURCE, cmdName, input);
    }

    private static void debugCommand(Command<TestSource> command) {
        System.out.println("Command '" + command.getName() + "' has usages: ");
        for (CommandUsage<TestSource> usage : command.getUsages()) {
            System.out.println("- " + CommandUsage.format(command, usage));
        }

    }

    @Test
    public void testTypeWrap() {
        final TypeWrap<List<String>> typeWrap = new TypeWrap<>() {
        };
        Assertions.assertEquals("java.util.List<java.lang.String>", typeWrap.getType().getTypeName());
    }

    @Test
    public void testTypeTolerantVerifierAmbiguity() {
        UsageVerifier<TestSource> verifier = UsageVerifier.typeTolerantVerifier();

        CommandUsage<TestSource> usage1 = CommandUsage.<TestSource>builder()
                .parameters(
                        CommandParameter.requiredText("arg1")
                ).build();

        CommandUsage<TestSource> usage2 = CommandUsage.<TestSource>builder()
                .parameters(
                        CommandParameter.requiredBoolean("arg2")
                ).build();

        Assertions.assertFalse(verifier.areAmbiguous(usage1, usage2));
    }

    @Test
    public void testIncompleteSubCommand() {
        //syntax -> /group <group> setperm <permission> [value]
        USAGE_EXECUTED = false;
        var result = testCmdTreeExecution("group", "member setperm");
        Assertions.assertEquals(UsageMatchResult.INCOMPLETE, result);
        Assertions.assertFalse(USAGE_EXECUTED);

    }

    @Test
    public void testCompleteSubCommand() {
        USAGE_EXECUTED = false;
        var result = testCmdTreeExecution("group", "member setperm command.group");
        Assertions.assertEquals(UsageMatchResult.COMPLETE, result);
        Assertions.assertTrue(USAGE_EXECUTED);
    }

    @Test
    public void testHelpSubCommand() {
        //syntax -> /group help [page]
        USAGE_EXECUTED = false;
        var result = testCmdTreeExecution("group", "help");
        Assertions.assertEquals(UsageMatchResult.COMPLETE, result);
        Assertions.assertTrue(USAGE_EXECUTED);
    }

    @Test
    public void testPreProcessor() {
        USAGE_EXECUTED = false;
        GROUP_CMD.setPreProcessor(new CustomPreProcessor());
        var result = testCmdTreeExecution("group", "member");

        Assertions.assertEquals(PRE_PROCESSOR_INT, 1);
        Assertions.assertEquals(UsageMatchResult.COMPLETE, result);
        Assertions.assertTrue(USAGE_EXECUTED);
    }

    @Test
    public void testPostProcessor() {
        USAGE_EXECUTED = false;
        GROUP_CMD.setPostProcessor(new CustomPostProcessor());

        var result = testCmdTreeExecution("group", "member");
        Assertions.assertEquals(POST_PROCESSOR_INT, 1);
        Assertions.assertEquals(UsageMatchResult.COMPLETE, result);
        Assertions.assertTrue(USAGE_EXECUTED);
    }

    @Test
    public void testSubInheritance() {
        System.out.println("----------------------------");
        debugCommand(Objects.requireNonNull(IMPERAT.getCommand("test")));
        debugCommand(Objects.requireNonNull(IMPERAT.getCommand("embedded")));
        
        var result = testCmdTreeExecution("test", "first-value first a1 a2 second a3");
        Assertions.assertEquals(UsageMatchResult.COMPLETE, result);
    }
    
    @Test
    public void testInnerClassParsing() {
        System.out.println("----------------------------");
        debugCommand(Objects.requireNonNull(IMPERAT.getCommand("test")));
        debugCommand(Objects.requireNonNull(IMPERAT.getCommand("embedded")));
        
        var result = testCmdTreeExecution("inner1", "");
        Assertions.assertEquals(UsageMatchResult.INCOMPLETE, result);
    }
    
    @Test
    public void testArgParsing() {
        ArgumentQueue queue = ArgumentQueue.parseAutoCompletion(new String[]{""});
        Assertions.assertEquals(1, queue.size());
    }
    
    @Test
    public void testAutoCompletion1() {
        var cmd = IMPERAT.getCommand("test");
        assert cmd != null;
        debugCommand(cmd);
        var results = IMPERAT.autoComplete(cmd, new TestSource(System.out), new String[]{""});
        Assertions.assertLinesMatch(Arrays.asList("hi", "bye"), results);
    }
    
    @Test
    public void testAutoCompletion2() {
        var cmd = IMPERAT.getCommand("test");
        assert cmd != null;
        debugCommand(cmd);
        var results = IMPERAT.autoComplete(cmd, new TestSource(System.out), new String[]{"hi", ""});
        Assertions.assertLinesMatch(Arrays.asList("othersub", "first"), results);
    }
    
    @Test
    public void testAutoCompletion3() {
        var cmd = IMPERAT.getCommand("test");
        assert cmd != null;
        debugCommand(cmd);
        var results = IMPERAT.autoComplete(cmd, new TestSource(System.out), new String[]{"hi", "first", ""});
        Assertions.assertLinesMatch(Arrays.asList("x", "y", "z", "sexy"), results);
    }
}
