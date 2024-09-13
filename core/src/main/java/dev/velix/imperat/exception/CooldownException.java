package dev.velix.imperat.exception;

import dev.velix.imperat.context.Source;
import lombok.Getter;

@Getter
public final class CooldownException extends ImperatException {

    private final long defaultCooldown, cooldown;

    public <S extends Source> CooldownException(final long cooldown, final long defaultCooldown) {
        this.defaultCooldown = defaultCooldown;
        this.cooldown = cooldown;
    }

}
