package studio.mevera.imperat.bukkit.test.commands;

import org.jetbrains.annotations.NotNull;
import studio.mevera.imperat.BukkitCommandSource;
import studio.mevera.imperat.command.arguments.Argument;
import studio.mevera.imperat.command.arguments.type.SimpleArgumentType;
import studio.mevera.imperat.context.CommandContext;
import studio.mevera.imperat.exception.CommandException;

/**
 * Test fixture: a fixed four-token argument type consuming
 * {@code <x> <y> <z> <w>}. Renders as four Brigadier segments so the
 * multi-token chain logic is exercised end-to-end.
 */
public final class QuadCoordArgumentType extends SimpleArgumentType<BukkitCommandSource, QuadCoord> {

    public QuadCoordArgumentType() {
        super(4);
    }

    @Override
    public QuadCoord parse(@NotNull CommandContext<BukkitCommandSource> context,
            @NotNull Argument<BukkitCommandSource> argument,
            @NotNull String input) throws CommandException {
        String[] parts = input.split(" ");
        if (parts.length != 4) {
            throw new CommandException("Expected '<x> <y> <z> <w>', got '" + input + "'");
        }
        try {
            return new QuadCoord(
                    Double.parseDouble(parts[0]),
                    Double.parseDouble(parts[1]),
                    Double.parseDouble(parts[2]),
                    Double.parseDouble(parts[3])
            );
        } catch (NumberFormatException ex) {
            throw new CommandException("Coordinates must be numbers: '" + input + "'");
        }
    }
}
