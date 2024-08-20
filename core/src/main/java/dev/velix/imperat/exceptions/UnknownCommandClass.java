package dev.velix.imperat.exceptions;

public final class UnknownCommandClass extends RuntimeException {

    public UnknownCommandClass(final Class<?> clazz) {
        super("Class '" + clazz.getName() + "' Doesn't have @Command ; couldn't recognize it as a command-class");
    }

}
