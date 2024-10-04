package dev.velix.imperat.exception;

public final class CooldownException extends ImperatException {
	
	private final long defaultCooldown, cooldown;
	
	public CooldownException(final long cooldown, final long defaultCooldown) {
		this.defaultCooldown = defaultCooldown;
		this.cooldown = cooldown;
	}
	
	public long getCooldown() {
		return cooldown;
	}
	
	public long getDefaultCooldown() {
		return defaultCooldown;
	}
	
}
