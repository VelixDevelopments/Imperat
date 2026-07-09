package studio.mevera.imperat.tests.enhanced;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import studio.mevera.imperat.command.Command;
import studio.mevera.imperat.command.CommandPathway;
import studio.mevera.imperat.command.arguments.Argument;
import studio.mevera.imperat.tests.TestCommandSource;
import studio.mevera.imperat.tests.TestImperat;
import studio.mevera.imperat.tests.TestImperatConfig;

/**
 * Programmatically-built pathways carry their execution in
 * {@link studio.mevera.imperat.command.CommandExecution}, not in an annotated
 * method element. Suggestion visibility must treat such pathways as
 * executable — otherwise builder-API subcommands execute fine but never show
 * up in tab completion.
 */
@DisplayName("Programmatic Subcommand Suggestion Tests")
class ProgrammaticSubcommandSuggestionTest {

    @Test
    @DisplayName("Should suggest programmatically-registered subcommands")
    void testProgrammaticSubcommandIsSuggested() {
        TestImperat imperat = TestImperatConfig.builder().build();

        Command<TestCommandSource> sub = Command.create(imperat, "child")
                                                 .pathway(CommandPathway.<TestCommandSource>builder()
                                                                  .arguments(Argument.requiredText("name"))
                                                                  .execute((source, ctx) -> {}))
                                                 .build();

        Command<TestCommandSource> parent = Command.create(imperat, "progparent")
                                                    .subCommand(sub)
                                                    .build();
        imperat.registerSimpleCommand(parent);

        var suggestions = imperat.autoComplete(new TestCommandSource(System.out), "progparent ").join();

        Assertions.assertThat(suggestions).contains("child");
    }
}
