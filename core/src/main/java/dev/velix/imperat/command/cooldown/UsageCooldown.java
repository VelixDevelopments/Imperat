package dev.velix.imperat.command.cooldown;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Represents a required cooldown of a {@link dev.velix.imperat.command.CommandUsage}
 * @see TimeUnit
 * @see Duration
 *
 * @param value the value for this
 * @param unit
 */
public record UsageCooldown(long value, TimeUnit unit) {

	public long toMillis() {
		return unit.toMillis(value);
	}

}
