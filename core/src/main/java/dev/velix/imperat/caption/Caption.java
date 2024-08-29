package dev.velix.imperat.caption;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.Source;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.context.Context;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a message
 */
@ApiStatus.AvailableSince("1.0.0")
public interface Caption<C> {

    /**
     * @return the key
     */
    @NotNull
    CaptionKey getKey();

    /**
     * @param dispatcher the command dispatcher
     * @param command    the command being triggered originally
     * @param source     the source
     * @param context    the context
     * @param usage      the command usage, can be null if it hasn't been resolved yet
     * @param exception  the exception, may be null if no exception provided
     * @return The message in the form of a component
     */
    @NotNull
    Component asComponent(
            @NotNull Imperat<C> dispatcher,
            @NotNull Command<C> command,
            @NotNull Source<C> source,
            @NotNull Context<C> context,
            @Nullable CommandUsage<C> usage,
            @Nullable Exception exception
    );


    default @NotNull Component asComponent(
            @NotNull Imperat<C> dispatcher,
            @NotNull Command<C> command,
            @NotNull Source<C> source,
            @NotNull Context<C> context,
            @Nullable CommandUsage<C> usage

    ) {
        return asComponent(dispatcher, command, source, context, usage, null);
    }

}
