package dev.velix.imperat;

import dev.velix.imperat.command.Description;
import dev.velix.imperat.command.DescriptionHolder;
import dev.velix.imperat.command.PermissionHolder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@ApiStatus.Internal
public final class WrappedBukkitCommand extends Command implements PermissionHolder, DescriptionHolder {

    private final dev.velix.imperat.command.Command<BukkitSource> imperatCommand;
    private final Command command;
    private Description description;

    private WrappedBukkitCommand(dev.velix.imperat.command.Command<BukkitSource> imperatCommand, @NotNull Command command) {
        super(command.getName(), command.getDescription(), command.getUsage(), command.getAliases());
        this.imperatCommand = imperatCommand;
        this.command = command;
        this.description = Description.of(command.getDescription());
    }

    static WrappedBukkitCommand wrap(dev.velix.imperat.command.Command<BukkitSource> imperatCommand, Command command) {
        return new WrappedBukkitCommand(imperatCommand, command);
    }

    public dev.velix.imperat.command.Command<BukkitSource> getImperatCommand() {
        return imperatCommand;
    }

    /**
     * Executes the command, returning its success
     *
     * @param sender       Source object which is executing this command
     * @param commandLabel The alias of the command used
     * @param args         All arguments passed to the command, split via ' '
     * @return true if the command was successful, otherwise false
     */
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        return command.execute(sender, commandLabel, args);
    }

    /**
     * Retrieves the current description associated with this entity.
     *
     * @return the current {@link Description}.
     */
    @Override
    public Description description() {
        return description;
    }

    /**
     * Sets the description for this entity.
     *
     * @param description the {@link Description} to set.
     */
    @Override
    public void describe(Description description) {
        this.description = description;
        setDescription(description.toString());
    }

    /**
     * Retrieves the permission associated with this holder.
     *
     * @return the permission string, or {@code null} if no permission is set.
     */
    @Override
    public @Nullable String permission() {
        return command.getPermission();
    }

    /**
     * Sets the permission for this holder.
     *
     * @param permission the permission string to set, can be {@code null}.
     */
    @Override
    public void permission(String permission) {
        command.setPermission(permission);
    }

    /**
     * Executed on tab completion for this command, returning a list of
     * options the player can tab through.
     *
     * @param sender Source object which is executing this command
     * @param alias  the alias being used
     * @param args   All arguments passed to the command, split via ' '
     * @return a list of tab-completions for the specified arguments. This
     * will never be null. List may be immutable.
     * @throws IllegalArgumentException if sender, alias, or args is null
     */
    @Override
    public @NotNull List<String> tabComplete(
        @NotNull CommandSender sender,
        @NotNull String alias,
        @NotNull String[] args
    ) throws IllegalArgumentException {
        return command.tabComplete(sender, alias, args);
    }
}
