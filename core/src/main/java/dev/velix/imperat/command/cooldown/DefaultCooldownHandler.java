package dev.velix.imperat.command.cooldown;

import dev.velix.imperat.CommandSource;
import dev.velix.imperat.command.CommandUsage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class DefaultCooldownHandler<C> implements CooldownHandler<C> {
	
	private final Map<C, Long> lastTimeExecuted = new HashMap<>();
	private final CommandUsage<C> usage;
	
	public DefaultCooldownHandler(CommandUsage<C> usage) {
		this.usage = usage;
	}
	
	
	/**
	 * Sets the last time of execution to this
	 * current moment using {@link System#currentTimeMillis()}
	 *
	 * @param source the command sender executing the {@link CommandUsage}
	 */
	@Override
	public void registerExecutionMoment(CommandSource<C> source) {
		lastTimeExecuted.put(source.getOrigin(), System.currentTimeMillis());
	}
	
	/**
	 * The required of a usage
	 *
	 * @return the container of usage's cooldown, the container may be empty
	 */
	@Override
	public Optional<UsageCooldown> getUsageCooldown() {
		return Optional.ofNullable(usage.getCooldown());
	}
	
	/**
	 * Unregisters the user's cached cooldown
	 * when it's expired !
	 *
	 * @param source the command-sender
	 */
	@Override
	public void removeCooldown(CommandSource<C> source) {
		lastTimeExecuted.remove(source.getOrigin());
	}
	
	/**
	 * Fetches the last time the command source
	 * executed a specific command usage
	 *
	 * @param source the command sender
	 * @return the last time the sender executed {@link CommandUsage}
	 */
	@Override
	public Optional<Long> getLastTimeExecuted(CommandSource<C> source) {
		return Optional.ofNullable(lastTimeExecuted.get(source.getOrigin()));
	}
}
