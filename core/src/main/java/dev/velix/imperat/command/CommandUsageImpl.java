package dev.velix.imperat.command;

import dev.velix.imperat.CommandSource;
import dev.velix.imperat.command.cooldown.CooldownHandler;
import dev.velix.imperat.command.cooldown.DefaultCooldownHandler;
import dev.velix.imperat.command.cooldown.UsageCooldown;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.CommandFlag;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.coordinator.CommandCoordinator;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@ApiStatus.Internal
final class CommandUsageImpl<C> implements CommandUsage<C> {
	
	private final static Pattern SINGLE_FLAG = Pattern.compile("-([a-zA-Z]+)");
	private final static Pattern DOUBLE_FLAG = Pattern.compile("--([a-zA-Z]+)");
	
	
	private String permission = null;
	private String description = "N/A";
	
	private @NotNull CooldownHandler<C> cooldownHandler;
	private @Nullable UsageCooldown cooldown = null;
	
	private final List<CommandParameter> parameters = new ArrayList<>();
	private final CommandExecution<C> execution;
	private CommandCoordinator<C> commandCoordinator;
	
	CommandUsageImpl(CommandExecution<C> execution) {
		this.execution = execution;
		this.cooldownHandler = new DefaultCooldownHandler<>(this);
		this.commandCoordinator = CommandCoordinator.sync();
	}
	
	/**
	 * @return the permission for this usage
	 */
	@Override
	public @Nullable String getPermission() {
		return permission;
	}
	
	/**
	 * The permission for this usage
	 *
	 * @param permission permission to set
	 */
	@Override
	public void setPermission(String permission) {
		this.permission = permission;
	}
	
	/**
	 * @return the description for the
	 * command usage
	 */
	@Override
	public String getDescription() {
		return description;
	}
	
	/**
	 * sets the description for the usage
	 *
	 * @param desc the description to set
	 */
	@Override
	public void setDescription(String desc) {
		this.description = desc;
	}
	
	/**
	 * Checks whether the raw input is a flag
	 * registered by this usage
	 *
	 * @param input the raw input
	 * @return Whether the input is a flag and is registered in the usage
	 */
	@Override
	public boolean hasFlag(String input) {
		return getFlagFromRaw(input) != null;
	}
	
	/**
	 * Fetches the flag from the input
	 *
	 * @param rawInput the input
	 * @return the flag from the raw input, null if it cannot be a flag
	 */
	@Override
	public @Nullable CommandFlag getFlagFromRaw(String rawInput) {
		boolean isSingle = SINGLE_FLAG.matcher(rawInput).matches();
		boolean isDouble = DOUBLE_FLAG.matcher(rawInput).matches();
		
		if(!isSingle && !isDouble) {
			return null;
		}
		
		String inputFlagAlias = rawInput.substring(isSingle ? 1 : 2);
		
		for(var param : parameters) {
			if(!param.isFlag()) continue;
			CommandFlag flag = param.asFlagParameter().getFlag();
			if(flag.name().equalsIgnoreCase(inputFlagAlias) ||
							flag.hasAlias(inputFlagAlias)) {
				return flag;
			}
		}
		
		return null;
	}
	
	
	/**
	 * Adds parameters to the usage
	 *
	 * @param params the parameters to add
	 */
	@Override
	public void addParameters(CommandParameter... params) {
		parameters.addAll(Arrays.asList(params));
	}
	
	/**
	 * Adds parameters to the usage
	 *
	 * @param params the parameters to add
	 */
	@Override
	public void addParameters(List<CommandParameter> params) {
		parameters.addAll(params);
	}
	
	/**
	 * @return the parameters for this usages
	 * @see CommandParameter
	 */
	@Override
	public List<CommandParameter> getParameters() {
		return parameters;
	}
	
	/**
	 * @return the execution for this command
	 */
	@Override
	public @NotNull CommandExecution<C> getExecution() {
		return execution;
	}
	
	/**
	 * @param clazz the type of the parameter to check upon
	 * @return Whether the usage has a specific type of parameter
	 */
	@Override
	public boolean hasParamType(Class<?> clazz) {
		return getParameters()
						.stream()
						.anyMatch((param) -> param.getType().equals(clazz));
	}
	
	/**
	 * @return Gets the minimal possible number
	 * of parameters that are acceptable to initiate this
	 * usage of a command.
	 */
	@Override
	public int getMinLength() {
		return (int) getParameters().stream()
						.filter((param) -> !param.isFlag())
						.filter((param) -> !param.isOptional())
						.count();
	}
	
	/**
	 * @return Gets the maximum possible number
	 * of parameters that are acceptable to initiate this
	 * usage of a command.
	 */
	@Override
	public int getMaxLength() {
		return getParameters().size();
	}
	
	/**
	 * Searches for a parameter with specific type
	 *
	 * @param parameterPredicate the parameter condition
	 * @return whether this usage has atLeast on {@link CommandParameter} with specific condition
	 * or not
	 */
	@Override
	public boolean hasParameter(Predicate<CommandParameter> parameterPredicate) {
		for (CommandParameter parameter : getParameters())
			if (parameterPredicate.test(parameter))
				return true;
		
		return false;
	}
	
	/**
	 * @param parameterPredicate the condition
	 * @return the parameter to get using a condition
	 */
	@Override
	public @Nullable CommandParameter getParameter(Predicate<CommandParameter> parameterPredicate) {
		for (CommandParameter parameter : getParameters()) {
			if (parameterPredicate.test(parameter))
				return parameter;
		}
		return null;
	}
	
	/**
	 * @return The cool down for this usage {@link UsageCooldown}
	 * returns null if no cooldown has been set
	 */
	@Override
	public @Nullable UsageCooldown getCooldown() {
		return cooldown;
	}
	
	/**
	 * Sets the command usage's cooldown {@link UsageCooldown}
	 *
	 * @param usageCooldown the cool down for this usage
	 */
	@Override
	public void setCooldown(@Nullable UsageCooldown usageCooldown) {
		this.cooldown = usageCooldown;
	}
	
	/**
	 * @return the cool down handler {@link CooldownHandler}
	 */
	@Override
	public @NotNull CooldownHandler<C> getCooldownHandler() {
		return cooldownHandler;
	}
	
	/**
	 * Sets the cooldown handler {@link CooldownHandler}
	 *
	 * @param cooldownHandler the cool down handler to set
	 */
	@Override
	public void setCooldownHandler(@NotNull CooldownHandler<C> cooldownHandler) {
		this.cooldownHandler = cooldownHandler;
	}
	
	/**
	 * @return the coordinator for execution of the command
	 */
	@Override
	public CommandCoordinator<C> getCoordinator() {
		return commandCoordinator;
	}
	
	/**
	 * Sets the command coordinator
	 *
	 * @param commandCoordinator the coordinator to set
	 */
	@Override
	public void setCoordinator(CommandCoordinator<C> commandCoordinator) {
		this.commandCoordinator = commandCoordinator;
	}
	
	/**
	 * Executes the usage's actions
	 * using the supplied {@link CommandCoordinator}
	 *
	 * @param source  the command source/sender
	 * @param context the context of the command
	 */
	@Override
	public void execute(CommandSource<C> source, Context<C> context) {
		commandCoordinator.coordinate(source, context, this.execution);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommandUsageImpl<?> that = (CommandUsageImpl<?>) o;
		return Objects.equals(parameters, that.parameters);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(parameters);
	}
}
