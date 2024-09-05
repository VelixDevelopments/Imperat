package dev.velix.imperat.help;

import dev.velix.imperat.command.CommandExecution;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.exceptions.CommandException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a help execution instructions
 *
 * @param <S> the type of sender
 */
@ApiStatus.AvailableSince("1.0.0")
public interface HelpExecution<S extends Source> extends CommandExecution<S> {

    String PAGE_PARAMETER_NAME = "page";

    /**
     * Displays a help menu showing all possible syntaxes
     *
     * @param source the source of this execution
     * @param help   the help object
     * @param page   the page of the help menu
     */
    void help(S source, Context<S> context,
              CommandHelp<S> help, @Nullable Integer page) throws CommandException;

    @Override
    default void execute(S source, ExecutionContext<S> context) throws CommandException {
        CommandHelp<S> help = context.createCommandHelp();
        help(source, (Context<S>) context, help, context.getArgument(PAGE_PARAMETER_NAME));
    }
}
