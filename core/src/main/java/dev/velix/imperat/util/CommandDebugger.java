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
        if(LOGGER == null) {
            System.out.println(String.format(msg, args));
            return;
        }
        LOGGER.log(Level.INFO, String.format(msg, args));
    }

    public static void warning(String msg, Object... args) {
        if(LOGGER == null) {
            System.out.println(String.format(msg, args));
            return;
        }
        LOGGER.log(Level.WARNING, String.format(msg, args));
    }

    public static void error(Class<?> owningClass, String name, Throwable ex) {
        if(LOGGER == null) {
            System.out.println(String.format("Error in class '%s', in method '%s'", owningClass.getName(), name));
            return;
        }
        LOGGER.log(Level.SEVERE, String.format("Error in class '%s', in method '%s'", owningClass.getName(), name), ex);
    }

    public static void debugParameters(String msg, List<CommandParameter> parameters) {
        if(LOGGER == null) {
            System.out.println(String.format(msg, parameters.stream().map(CommandParameter::format)
                    .collect(Collectors.joining(","))));
            return;
        }
        LOGGER.log(Level.INFO, String.format(msg, parameters.stream().map(CommandParameter::format)
                .collect(Collectors.joining(","))));
    }

}