package dev.velix.imperat.command;

import dev.velix.imperat.command.cooldown.CooldownHandler;
import dev.velix.imperat.command.cooldown.DefaultCooldownHandler;
import dev.velix.imperat.command.cooldown.UsageCooldown;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@ApiStatus.Internal
final class CommandUsageImpl<C> implements CommandUsage<C> {

	private String permission = null;
	private String description = "N/A";

	private @NotNull CooldownHandler<C> cooldownHandler;
	private @Nullable UsageCooldown cooldown = null;

	private final List<UsageParameter> parameters = new ArrayList<>();
	private final CommandExecution<C> execution;

	CommandUsageImpl(CommandExecution<C> execution) {
		this.execution = execution;
		this.cooldownHandler = new DefaultCooldownHandler<>(this);
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
	 * Adds parameters to the usage
	 *
	 * @param params the parameters to add
	 */
	@Override
	public void addParameters(UsageParameter... params) {
		parameters.addAll(Arrays.asList(params));
	}

	/**
	 * Adds parameters to the usage
	 *
	 * @param params the parameters to add
	 */
	@Override
	public void addParameters(List<UsageParameter> params) {
		parameters.addAll(params);
	}

	/**
	 * @return the parameters for this usages
	 * @see UsageParameter
	 */
	@Override
	public List<UsageParameter> getParameters() {
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
	 * @return whether this usage has atLeast on {@link UsageParameter} with specific condition
	 * or not
	 */
	@Override
	public boolean hasParameter(Predicate<UsageParameter> parameterPredicate) {
		for (UsageParameter parameter : getParameters())
			if (parameterPredicate.test(parameter))
				return true;

		return false;
	}

	/**
	 * @param parameterPredicate the condition
	 * @return the parameter to get using a condition
	 */
	@Override
	public @Nullable UsageParameter getParameter(Predicate<UsageParameter> parameterPredicate) {
		for (UsageParameter parameter : getParameters()) {
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
