package dev.velix.imperat.command;


import dev.velix.imperat.command.cooldown.UsageCooldown;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

public interface CooldownHolder {

    @Nullable
    UsageCooldown getCooldown();

    void setCooldown(UsageCooldown cooldown);

    default void setCooldown(long value, TimeUnit unit) {
        setCooldown(value, unit, null);
    }

    default void setCooldown(long value, TimeUnit unit, @Nullable String permission) {
        setCooldown(new UsageCooldown(value, unit, permission));
    }

}
