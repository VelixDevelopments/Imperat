package dev.velix.imperat;

import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.tree.TraverseResult;
import dev.velix.imperat.util.TypeWrap;
import dev.velix.imperat.verification.UsageVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.velix.imperat.TestCommands.GROUP_CMD;
import static dev.velix.imperat.TestCommands.MULTIPLE_OPTIONAL_CMD;

public class TestRun {

    TestRun() {
    }

    private final static TestImperat IMPERAT = new TestImperat();
    private final static TestSender SOURCE = new TestSender(System.out);

    static {
        IMPERAT.setUsageVerifier(UsageVerifier.typeTolerantVerifier());

        IMPERAT.registerCommand(GROUP_CMD);
        IMPERAT.registerCommand(MULTIPLE_OPTIONAL_CMD);
    }

    private static TraverseResult testCmdTreeExecution(String cmdName, String input) {
        return IMPERAT.dispatch(SOURCE, cmdName, input);
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
        //syntax -> /group <group> setperm <permission>
        var result = testCmdTreeExecution("group", "member setperm");
        Assertions.assertEquals(TraverseResult.INCOMPLETE, result);
    }

    @Test
    public void testHelpSubCommand() {
        //syntax -> /group help [page]
        var result = testCmdTreeExecution("group", "help");
        Assertions.assertEquals(TraverseResult.COMPLETE, result);
    }

}
