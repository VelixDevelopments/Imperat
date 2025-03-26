package dev.velix.imperat.context;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.ImperatConfig;
import dev.velix.imperat.command.Command;
import org.jetbrains.annotations.*;

/**
 * Represents the processes context of a command
 * entered by {@link Source}
 *
 * @param <S> the command sender valueType
 */
@ApiStatus.AvailableSince("1.0.0")
public interface Context<S extends Source> {

    Imperat<S> imperat();

    ImperatConfig<S> imperatConfig();

    /**
     * @return The {@link Command} owning this context.
     */
    Command<S> command();

    /**
     * @return the {@link Source} of the command
     * @see Source
     */
    @NotNull
    S source();

    /**
     * @return the arguments entered by the {@link Source}
     * @see ArgumentQueue
     */
    ArgumentQueue arguments();


}
