package dev.velix.imperat.command;

import dev.velix.imperat.command.suggestions.AutoCompleter;
import dev.velix.imperat.context.internal.FlagRegistry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

@ApiStatus.Internal
final class CommandImpl<C> implements Command<C> {
	
	private final String name;
	private String permission = null;
	private String description = "N/A";
	
	private int position = -1;
	
	private boolean suppressACPermissionChecks = false;
	
	private final List<String> aliases = new ArrayList<>();
	
	private CommandUsage<C> defaultUsage;
	
	private final FlagRegistry flagRegistry;
	
	private final Command<C> parent;
	private final Map<String, Command<C>> children = new HashMap<>();
	private final AutoCompleter<C> autoCompleter;
	private final Set<CommandUsage<C>> usages = new LinkedHashSet<>();
	
	
	CommandImpl(String name) {
		this(null, name);
	}
	
	//sub-command constructor
	CommandImpl(@Nullable Command<C> parent, String name) {
		this.parent = parent;
		this.name = name;
		this.flagRegistry = new FlagRegistry();
		setDefaultUsageExecution((source, context) -> {
		});
		this.autoCompleter = AutoCompleter.createNative(this);
	}
	
	
	/**
	 * @return the name of the command
	 */
	@Override
	public String getName() {
		return name;
	}
	
	/**
	 * @return The permission of the command
	 */
	@Override
	public @Nullable String getPermission() {
		return permission;
	}
	
	/**
	 * Sets the permission of a command
	 *
	 * @param permission the permission of a command
	 */
	@Override
	public void setPermission(@Nullable String permission) {
		this.permission = permission;
	}
	
	/**
	 * @return The  description of a command
	 */
	@Override
	public @NotNull String getDescription() {
		return description;
	}
	
	/**
	 * Sets the description of a command
	 *
	 * @param description the desc to set
	 */
	@Override
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	/**
	 * @return the index of this parameter
	 */
	@Override
	public int getPosition() {
		return position;
	}
	
	/**
	 * Sets the position of this command in a syntax
	 * DO NOT USE THIS FOR ANY REASON unless it's necessary to do so
	 *
	 * @param position the position to set
	 */
	@Override
	public void setPosition(int position) {
		this.position = position;
	}
	
	
	/**
	 * @return the aliases for this commands
	 */
	@Override
	public @UnmodifiableView List<String> getAliases() {
		return aliases;
	}
	
	/**
	 * Sets the aliases of a command
	 *
	 * @param aliases the aliases for te command to set
	 */
	@Override
	public void addAliases(List<String> aliases) {
		this.aliases.addAll(aliases);
	}
	
	/**
	 * @return the default usage of the command
	 * without any args
	 */
	@Override
	public @NotNull CommandUsage<C> getDefaultUsage() {
		return defaultUsage;
	}
	
	/**
	 * @param execution sets what happens when there's no parameters
	 */
	@Override
	public void setDefaultUsageExecution(CommandExecution<C> execution) {
		this.defaultUsage = CommandUsage.<C>builder()
						.execute(execution)
						.build();
	}
	
	/**
	 * The flags that are  registered
	 * to be usable in this command's syntax
	 *
	 * @return the registered flags for this command
	 */
	@Override
	public @NotNull FlagRegistry getKnownFlags() {
		return flagRegistry;
	}
	
	/**
	 * Adds a usage to the command
	 *
	 * @param usage the usage {@link CommandUsage} of the command
	 */
	@Override
	public void addUsage(CommandUsage<C> usage) {
		usages.add(usage);
	}
	
	/**
	 * @return all {@link CommandUsage} that were registered
	 * to this command by the user
	 */
	@Override
	public Collection<? extends CommandUsage<C>> getUsages() {
		return usages;
	}
	
	/**
	 * @return the usage that doesn't include any subcommands , only
	 * parameters
	 */
	@Override
	public @NotNull CommandUsage<C> getMainUsage() {
		return usages.stream().filter((usage ->
										usage.getMinLength() >= 1 &&
														!usage.hasParamType(Command.class)))
						.findFirst().orElse(defaultUsage);
	}
	
	/**
	 * @return Returns {@link AutoCompleter}
	 * that handles all auto-completes for this command
	 */
	@Override
	public AutoCompleter<C> getAutoCompleter() {
		return autoCompleter;
	}
	
	/**
	 * @return Whether this command is a sub command or not
	 */
	@Override
	public @Nullable Command<C> getParent() {
		return parent;
	}
	
	/**
	 * The command to be added as a subcommand of this instance
	 *
	 * @param command the sub-command to be added
	 */
	@Override
	public void addSubCommand(Command<C> command) {
		children.put(command.getName(), command);
	}
	
	/**
	 * @param name the name of the wanted sub-command
	 * @return the sub-command of specific name directly from
	 * this instance of a command, meaning that
	 * it won't go deeply in search for sub-command
	 */
	@Override
	public @Nullable Command<C> getSubCommand(String name) {
		Command<C> sub = children.get(name);
		if (sub != null)
			return sub;
		
		for (String subsNames : children.keySet()) {
			Command<C> other = children.get(subsNames);
			if (other.hasName(name)) return other;
			else if (subsNames.startsWith(name)) return other;
		}
		return null;
	}
	
	/**
	 * @return the subcommands of this command
	 */
	@Override
	public @NotNull Collection<? extends Command<C>> getSubCommands() {
		return children.values();
	}
	
	/**
	 * whether to ignore permission checks on the auto-completion of command and
	 * sub commands or not
	 *
	 * @return whether to ignore permission checks on the auto-completion of command and
	 * sub commands or not
	 */
	@Override
	public boolean isIgnoringACPerms() {
		return suppressACPermissionChecks;
	}
	
	/**
	 * if true , it will ignore permission checks
	 * on the auto-completion of command and sub commands
	 * <p>
	 * otherwise, it will perform permission checks and
	 * only tab-completes the usages/subcommands that you have permission for
	 *
	 * @param suppress true if you want to ignore the permission checks on tab completion of args
	 */
	@Override
	public void ignoreACPermissions(boolean suppress) {
		this.suppressACPermissionChecks = suppress;
	}
	
	
}
