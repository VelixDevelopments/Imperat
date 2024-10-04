package dev.velix.imperat.exception;

public class InvalidUUIDException extends ImperatException {
	
	private final String raw;
	
	public InvalidUUIDException(final String raw) {
		this.raw = raw;
	}
	
	public String getRaw() {
		return raw;
	}
	
}
