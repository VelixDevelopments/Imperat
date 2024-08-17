package dev.velix.imperat.exceptions;

public final class UnknownCommandClass extends RuntimeException {
	
	public UnknownCommandClass(Class<?> clazz) {
		super("Class '" + clazz.getName() + "' Doesn't have @Command ; couldn't recognize it as a command-class");
	}
	
}
