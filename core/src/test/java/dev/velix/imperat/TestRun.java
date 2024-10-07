package dev.velix.imperat;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.tree.CommandDispatch;
import dev.velix.imperat.commands.annotations.examples.BanCommand;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.util.TypeWrap;
import dev.velix.imperat.verification.UsageVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static dev.velix.imperat.commands.TestCommands.GROUP_CMD;
import static dev.velix.imperat.commands.TestCommands.MULTIPLE_OPTIONAL_CMD;

public class TestRun {

    TestRun() {
        USAGE_EXECUTED = false;
    }

    final static TestImperat IMPERAT = new TestImperat();
    final static TestSource SOURCE = new TestSource(System.out);

    public static volatile boolean USAGE_EXECUTED = false;
    public static volatile int POST_PROCESSOR_INT = 0;
    public static volatile int PRE_PROCESSOR_INT = 0;

    static {
        IMPERAT.setUsageVerifier(UsageVerifier.typeTolerantVerifier());

        //IMPERAT.registerCommand(GROUP_CMD);
        IMPERAT.registerCommand(MULTIPLE_OPTIONAL_CMD);
        //IMPERAT.registerCommand(CHAINED_SUBCOMMANDS_CMD);
        //IMPERAT.registerCommand(new AnnotatedGroupCommand());
        //IMPERAT.registerCommand(new TestCommand());
        //IMPERAT.registerCommand(new OptionalArgCommand());
        IMPERAT.registerCommand(new BanCommand());

    }

    private static CommandDispatch.Result testCmdTreeExecution(String cmdName, String input) {
        return IMPERAT.dispatch(SOURCE, cmdName, input);
    }

    private static void debugCommand(Command<TestSource> command) {
        command.visualizeTree();

        System.out.println("Debugging sub commands: ");
        System.out.println("Command '" + command.name() + "' has usages: ");
        for (CommandUsage<TestSource> usage : command.usages()) {
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

        CommandUsage.Builder<TestSource> usage1 = CommandUsage.<TestSource>builder()
            .parameters(
                CommandParameter.requiredText("arg1")
            );

        CommandUsage.Builder<TestSource> usage2 = CommandUsage.<TestSource>builder()
            .parameters(
                CommandParameter.requiredBoolean("arg2")
            );

        Assertions.assertFalse(verifier.areAmbiguous(usage1.build(GROUP_CMD), usage2.build(GROUP_CMD)));
    }

    @Test
    public void testHelp() {
        debugCommand(Objects.requireNonNull(IMPERAT.getCommand("test")));
        Assertions.assertEquals(CommandDispatch.Result.COMPLETE, testCmdTreeExecution("test", "help"));
    }

    @Test
    public void testIncompleteSubCommand() {
        //syntax -> /group <group> setperm <permission> [value]
        var result = testCmdTreeExecution("group", "member setperm");
        Assertions.assertEquals(CommandDispatch.Result.INCOMPLETE, result);
    }

    @Test
    public void testCompleteSubCommand() {
        var result = testCmdTreeExecution("group", "member setperm command.group");
        Assertions.assertEquals(CommandDispatch.Result.COMPLETE, result);
    }

    @Test
    public void testHelpSubCommand() {
        //syntax -> /group help [page]
        var result = testCmdTreeExecution("group", "help");
        Assertions.assertEquals(CommandDispatch.Result.COMPLETE, result);
    }
    
    /*@Test
    public void testPreProcessor() {
        USAGE_EXECUTED = false;
        GROUP_CMD.setPreProcessor(new CustomPreProcessor());
        var result = testCmdTreeExecution("group", "member");
        
        Assertions.assertEquals(PRE_PROCESSOR_INT, 1);
        Assertions.assertEquals(Result.COMPLETE, result);
        Assertions.assertTrue(USAGE_EXECUTED);
    }
    
    @Test
    public void testPostProcessor() {
        USAGE_EXECUTED = false;
        GROUP_CMD.setPostProcessor(new CustomPostProcessor());
        
        var result = testCmdTreeExecution("group", "member");
        Assertions.assertEquals(POST_PROCESSOR_INT, 1);
        Assertions.assertEquals(Result.COMPLETE, result);
        Assertions.assertTrue(USAGE_EXECUTED);
    }*/

    @Test
    public void testSubInheritance() {
        System.out.println("----------------------------");
        debugCommand(Objects.requireNonNull(IMPERAT.getCommand("test")));
        debugCommand(Objects.requireNonNull(IMPERAT.getCommand("embedded")));

        var result = testCmdTreeExecution("test", "first-value first a1 second a3");
        Assertions.assertEquals(CommandDispatch.Result.COMPLETE, result);
    }

    @Test
    public void testInnerClassParsing() {
        System.out.println("----------------------------");
        debugCommand(Objects.requireNonNull(IMPERAT.getCommand("test")));
        //debugCommand(Objects.requireNonNull(IMPERAT.getCommand("embedded")));

        Assertions.assertEquals(CommandDispatch.Result.INCOMPLETE, testCmdTreeExecution("test", "text sub1"));
        Assertions.assertEquals(CommandDispatch.Result.COMPLETE, testCmdTreeExecution("test", "text sub1 hi"));
        Assertions.assertEquals(CommandDispatch.Result.INCOMPLETE, testCmdTreeExecution("test", "text sub1 hi sub2"));
        Assertions.assertEquals(CommandDispatch.Result.COMPLETE, testCmdTreeExecution("test", "text sub1 hi sub2 bye"));
        Assertions.assertEquals(CommandDispatch.Result.INCOMPLETE, testCmdTreeExecution("test", "text sub1 hi sub2 bye sub3"));
        Assertions.assertEquals(CommandDispatch.Result.COMPLETE, testCmdTreeExecution("test", "text sub1 hi sub2 bye sub3 hello"));

        Assertions.assertEquals(CommandDispatch.Result.INCOMPLETE, testCmdTreeExecution("test", "text sub4"));
        Assertions.assertEquals(CommandDispatch.Result.COMPLETE, testCmdTreeExecution("test", "text sub4 hi"));
        Assertions.assertEquals(CommandDispatch.Result.INCOMPLETE, testCmdTreeExecution("test", "text sub4 hi sub5"));
        Assertions.assertEquals(CommandDispatch.Result.COMPLETE, testCmdTreeExecution("test", "text sub4 hi sub5 bye"));
        Assertions.assertEquals(CommandDispatch.Result.INCOMPLETE, testCmdTreeExecution("test", "text sub4 hi sub5 bye sub6"));
        Assertions.assertEquals(CommandDispatch.Result.COMPLETE, testCmdTreeExecution("test", "text sub4 hi sub5 bye sub6 hello"));

    }

    @Test
    public void testArgParsing() {
        ArgumentQueue queue = ArgumentQueue.parseAutoCompletion(new String[]{""}, true);
        Assertions.assertEquals(1, queue.size());
    }

    @Test
    public void testAutoCompletion1() {
        var cmd = IMPERAT.getCommand("test");
        assert cmd != null;
        debugCommand(cmd);
        var results = IMPERAT.autoComplete(cmd, new TestSource(System.out), new String[]{""});
        results.whenComplete((res, ex) -> {
            Assertions.assertLinesMatch(Stream.of("hi", "bye"), res.stream());
        });
    }

    @Test
    public void testAutoCompletion2() {
        var cmd = IMPERAT.getCommand("test");
        assert cmd != null;
        debugCommand(cmd);
        var results = IMPERAT.autoComplete(cmd, new TestSource(System.out), new String[]{"hi", ""});
        results.whenComplete((res, ex) -> {
            Assertions.assertLinesMatch(Stream.of("othersub", "first", "sub4", "sub1"), res.stream());
        });
    }

    @Test
    public void testAutoCompletion3() {
        var cmd = IMPERAT.getCommand("test");
        assert cmd != null;
        debugCommand(cmd);
        var results = IMPERAT.autoComplete(cmd, new TestSource(System.out), new String[]{"hi", "first", ""});
        results.whenComplete((res, ex) -> {
            Assertions.assertLinesMatch(Stream.of("x", "y", "z", "sexy"), res.stream());
        });
    }

    @Test
    public void testOptionalArgCmd() {
        var cmd = IMPERAT.getCommand("opt");
        assert cmd != null;
        debugCommand(cmd);
        Assertions.assertEquals(CommandDispatch.Result.COMPLETE, testCmdTreeExecution("opt", "hi"));
    }

    @Test
    public void testBanCmd() {
        var cmd = IMPERAT.getCommand("ban");
        assert cmd != null;
        debugCommand(cmd);

        Assertions.assertEquals(CommandDispatch.Result.COMPLETE, testCmdTreeExecution("ban", "mqzen"));
    }

    @Test
    public void testBanWithFlag() {
        var cmd = IMPERAT.getCommand("ban");
        assert cmd != null;
        debugCommand(cmd);

        Assertions.assertEquals(CommandDispatch.Result.COMPLETE, testCmdTreeExecution("ban", "mqzen -s"));
    }

    @Test
    public void testUpperCaseCommandName() {
        IMPERAT.registerCommand(Command.create("UPPER_CAsE")
            .defaultExecution((src, ctx) -> src.reply("Worked !"))
            .build());
        Assertions.assertEquals(CommandDispatch.Result.INCOMPLETE, testCmdTreeExecution("upper_case", ""));
    }
}
