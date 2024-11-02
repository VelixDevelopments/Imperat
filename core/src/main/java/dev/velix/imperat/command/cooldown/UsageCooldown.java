package dev.velix.imperat.command.cooldown;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Represents a required cooldown of a {@link dev.velix.imperat.command.CommandUsage}
 *
 * @param value the value for this
 * @param unit  the unit for the time
 * @see TimeUnit
 * @see Duration
 */
@ApiStatus.AvailableSince("1.0.0")
public record UsageCooldown(long value, TimeUnit unit, @Nullable String permission) {

    public long toMillis() {
        return unit.toMillis(value);
    }

}
