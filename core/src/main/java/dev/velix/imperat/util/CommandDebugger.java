package dev.velix.imperat.util;

import dev.velix.imperat.command.parameters.CommandParameter;

import java.util.List;
import java.util.logging.*;
import java.util.stream.Collectors;

public final class CommandDebugger {

    private static Logger LOGGER;

    public static void setLogger(Logger LOGGER) {
        CommandDebugger.LOGGER = LOGGER;
    }

    private CommandDebugger() {
    }

    public static void debug(String msg, Object... args) {
        LOGGER.log(Level.INFO, String.format(msg, args));
    }

    public static void warning(String msg, Object... args) {
        LOGGER.log(Level.WARNING, String.format(msg, args));
    }

    public static void error(Class<?> owningClass, String name, Throwable ex) {
        LOGGER.log(Level.SEVERE, String.format("Error in class '%s', in method '%s'", owningClass.getName(), name), ex);
    }

    public static void debugParameters(String msg, List<CommandParameter> parameters) {
        LOGGER.log(Level.INFO, String.format(msg, parameters.stream().map(CommandParameter::format)
                .collect(Collectors.joining(","))));
    }

}
