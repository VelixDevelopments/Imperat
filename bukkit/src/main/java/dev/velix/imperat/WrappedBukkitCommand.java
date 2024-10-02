package dev.velix.imperat;

import dev.velix.imperat.command.Description;
import dev.velix.imperat.command.DescriptionHolder;
import dev.velix.imperat.command.PermissionHolder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
}
