package dev.velix.imperat.command;

import dev.velix.imperat.command.cooldown.CooldownHandler;
import dev.velix.imperat.command.cooldown.UsageCooldown;
import dev.velix.imperat.command.parameters.UsageParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Represents a usage of a command
 * that can be used in the future during an execution
 *
 * @see Command
 */
public interface CommandUsage<C>{

	/**
	 * @return the permission for this usage
	 */
	@Nullable
	String getPermission();

	/**
	 * The permission for this usage
	 *
	 * @param permission permission to set
	 */
	void setPermission(@Nullable String permission);

	/**
	 * @return the description for the
	 * command usage
	 */
	String getDescription();

	/**
	 * sets the description for the usage
	 *
	 * @param desc the description to set
	 */
	void setDescription(String desc);

	/**
	 * Adds parameters to the usage
	 *
	 * @param params the parameters to add
	 */
	void addParameters(UsageParameter... params);

	/**
	 * Adds parameters to the usage
	 *
	 * @param params the parameters to add
	 */
	void addParameters(List<UsageParameter> params);

	/**
	 * @return the parameters for this usages
	 * @see UsageParameter
	 */
	List<UsageParameter> getParameters();

	/**
	 * @return the execution for this usage
	 */
	@NotNull
	CommandExecution<C> getExecution();


	/**
	 * Creates a new command usage instance to use it to Merge
	 * this usage with the usage regarding parameters
	 * and takes the target's execution as well
	 *
	 * @param usage the usage to merge with
	 * @return the merged command usage !
	 */
	default CommandUsage<C> merge(CommandUsage<C> usage) {
		List<UsageParameter> parameters = new ArrayList<>(this.getParameters());
		parameters.addAll(usage.getParameters());

		return CommandUsage.<C>builder()
				  .parameters(parameters)
				  .execute(usage.getExecution())
				  .build();
	}

	/**
	 * Creates a new command usage instance to use it to Merge
	 * this usage with the usage regarding parameters
	 * and takes the target's execution as well
	 *
	 * <p>
	 * it also it also includes the subcommand's parameter itself
	 * into the command's usage !
	 * </p>
	 *
	 * @param subCommand the subcommand owning that usage
	 * @param usage      the usage to merge with
	 * @return the merged command usage !
	 */
	default CommandUsage<C> mergeWithCommand(Command<C> subCommand, CommandUsage<C> usage) {
		List<UsageParameter> parameters = new ArrayList<>(this.getParameters());
		parameters.add(subCommand);
		parameters.addAll(usage.getParameters());

		return CommandUsage.<C>builder()
				  .cooldown(usage.getCooldown())
				  .parameters(parameters)
				  .execute(usage.getExecution())
				  .build();
	}


	static <C> String format(Command<C> command, CommandUsage<C> usage) {
		StringBuilder builder = new StringBuilder(command.getName() + " ");
		int i = 0;
		for (UsageParameter parameter : usage.getParameters()) {
			builder.append(parameter.format(command));
			if (i != usage.getParameters().size() - 1) {
				builder.append(" ");
			}
			i++;
		}
		return builder.toString();
	}

	static <C> Builder<C> builder() {
		return new Builder<>();
	}

	/**
	 * @param clazz the type of the parameter to check upon
	 * @return Whether the usage has a specific type of parameter
	 */
	boolean hasParamType(Class<?> clazz);

	/**
	 * @return Gets the minimal possible number
	 * of parameters that are acceptable to initiate this
	 * usage of a command.
	 */
	int getMinLength();

	/**
	 * @return Gets the maximum possible number
	 * of parameters that are acceptable to initiate this
	 * usage of a command.
	 */
	int getMaxLength();

	/**
	 * Searches for a parameter with specific type
	 *
	 * @param parameterPredicate the parameter condition
	 * @return whether this usage has atLeast on {@link UsageParameter} with specific condition
	 * or not
	 */
	boolean hasParameter(Predicate<UsageParameter> parameterPredicate);

	/**
	 * @param parameterPredicate the condition
	 * @return the parameter to get using a condition
	 */
	@Nullable
	UsageParameter getParameter(Predicate<UsageParameter> parameterPredicate);

	/**
	 * @return The cool down for this usage {@link UsageCooldown}
	 * returns null if no cooldown has been set
	 */
	@Nullable
	UsageCooldown getCooldown();


	/**
	 * Sets the command usage's cooldown {@link UsageCooldown}
	 *
	 * @param usageCooldown the cool down for this usage
	 */
	void setCooldown(UsageCooldown usageCooldown);

	/**
	 * @return the cool down handler {@link CooldownHandler}
	 */
	@NotNull
	CooldownHandler<C> getCooldownHandler();

	/**
	 * Sets the cooldown handler {@link CooldownHandler}
	 *
	 * @param cooldownHandler the cool down handler to set
	 */
	void setCooldownHandler(CooldownHandler<C> cooldownHandler);

	default boolean isDefault() {
		return getParameters().isEmpty();
	}



	class Builder<C> {

		private CommandExecution<C> execution;
		private String description = "N/A";
		private String permission = null;
		private final List<UsageParameter> parameters = new ArrayList<>();
		private UsageCooldown cooldown = null;

		Builder() {

		}

		public Builder<C> execute(CommandExecution<C> execution) {
			this.execution = execution;
			return this;
		}

		public Builder<C> permission(String permission) {
			this.permission = permission;
			return this;
		}

		public Builder<C> cooldown(long value, TimeUnit unit) {
			this.cooldown = new UsageCooldown(value, unit);
			return this;
		}

		public Builder<C> cooldown(@Nullable UsageCooldown cooldown) {
			this.cooldown = cooldown;
			return this;
		}

		public Builder<C> description(String description) {
			if (description != null) {
				this.description = description;
			}
			return this;
		}

		public Builder<C> parameters(UsageParameter... params) {
			int index = 0;
			for (UsageParameter parameter : params) {
				parameter.setPosition(index);
				this.parameters.add(parameter);
				index++;
			}
			return this;
		}

		public Builder<C> parameters(List<UsageParameter> params) {
			for (int i = 0; i < params.size(); i++) {
				UsageParameter parameter = params.get(i);
				parameter.setPosition(i);
				this.parameters.add(parameter);
			}
			return this;
		}

		public CommandUsage<C> build() {
			CommandUsageImpl<C> impl = new CommandUsageImpl<>(execution);
			impl.setPermission(permission);
			impl.setDescription(description);
			impl.setCooldown(cooldown);
			impl.addParameters(parameters);
			return impl;
		}

	}
}
