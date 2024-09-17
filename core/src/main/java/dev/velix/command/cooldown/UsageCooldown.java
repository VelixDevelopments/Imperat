package dev.velix.command.cooldown;

import dev.velix.command.CommandUsage;
import org.jetbrains.annotations.ApiStatus;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Represents a required cooldown of a {@link CommandUsage}
 *
 * @param value the value for this
 * @param unit  the unit for the time
 * @see TimeUnit
 * @see Duration
 */
@ApiStatus.AvailableSince("1.0.0")
public record UsageCooldown(long value, TimeUnit unit) {
    
    public long toMillis() {
        return unit.toMillis(value);
    }
    
}
