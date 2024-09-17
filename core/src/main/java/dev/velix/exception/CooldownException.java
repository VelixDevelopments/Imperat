package dev.velix.exception;

import lombok.Getter;

@Getter
public final class CooldownException extends ImperatException {

    private final long defaultCooldown, cooldown;
    
    public CooldownException(final long cooldown, final long defaultCooldown) {
        this.defaultCooldown = defaultCooldown;
        this.cooldown = cooldown;
    }

}
