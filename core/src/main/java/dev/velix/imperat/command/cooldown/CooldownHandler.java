package dev.velix.imperat.command.cooldown;

import dev.velix.imperat.CommandSource;
import dev.velix.imperat.command.CommandUsage;

import java.util.Optional;

/**
 * Cool-down checker and handler for the command usages
 * {@link CommandUsage}
 *
 * @param <C> the sender-type
 */
public interface CooldownHandler<C> {


	/**
	 * Sets the last time of execution to this
	 * current moment using {@link System#currentTimeMillis()}
	 *
	 * @param source the command sender executing the {@link CommandUsage}
	 */
	void registerExecutionMoment(CommandSource<C> source);

	/**
	 * The required of a usage
	 *
	 * @return the container of usage's cooldown, the container may be empty
	 */
	Optional<UsageCooldown> getUsageCooldown();

	/**
	 * Checks if there's a cooldown on
	 * the usage for a specific command sender
	 *
	 * @param source the command sender/source
	 * @return whether there's a current cooldown
	 * on the usage for the command sender
	 */
	default boolean hasCooldown(CommandSource<C> source) {
		UsageCooldown usageCooldown = getUsageCooldown().orElse(null);
		if (usageCooldown == null) {
			return false;
		}

		boolean result = getLastTimeExecuted(source).map((lastTime) -> {
			long timePassed = System.currentTimeMillis() - lastTime;
			return timePassed <= usageCooldown.toMillis();
		}).orElse(false);

		if (!result)
			removeCooldown(source);
		return result;
	}

	/**
	 * Unregisters the user's cached cooldown
	 * when it's expired !
	 *
	 * @param source the command-sender
	 */
	void removeCooldown(CommandSource<C> source);


	/**
	 * Fetches the last time the command source
	 * executed a specific command usage
	 *
	 * @param source the command sender
	 * @return the last time the sender executed {@link CommandUsage}
	 */
	Optional<Long> getLastTimeExecuted(CommandSource<C> source);
}
