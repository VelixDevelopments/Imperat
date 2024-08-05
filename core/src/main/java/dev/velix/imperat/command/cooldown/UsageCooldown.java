package dev.velix.imperat.command.cooldown;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Represents a required cooldown of a {@link dev.velix.imperat.command.CommandUsage}
 *
 * @param value the value for this
 * @param unit
 * @see TimeUnit
 * @see Duration
 */
public record UsageCooldown(long value, TimeUnit unit) {

	public long toMillis() {
		return unit.toMillis(value);
	}

}
