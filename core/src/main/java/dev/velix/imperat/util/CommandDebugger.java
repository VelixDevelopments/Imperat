package dev.velix.imperat.util;

import dev.velix.imperat.command.parameters.CommandParameter;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class CommandDebugger {

    private static Logger LOGGER;

    private CommandDebugger() {
    }

    public static void setLogger(Logger LOGGER) {
        CommandDebugger.LOGGER = LOGGER;
    }

    public static void debug(String msg, Object... args) {
        if (LOGGER == null) {
            System.out.printf((msg) + "%n", args);
            return;
        }
        LOGGER.log(Level.INFO, String.format(msg, args));
    }

    public static void warning(String msg, Object... args) {
        if (LOGGER == null) {
            System.out.printf((msg) + "%n", args);
            return;
        }
        LOGGER.log(Level.WARNING, String.format(msg, args));
    }

    public static void error(Class<?> owningClass, String name, Throwable ex) {
        if (LOGGER == null) {
            System.out.printf("Error in class '%s', in method '%s'%n", owningClass.getName(), name);
            return;
        }
        LOGGER.log(Level.SEVERE, String.format("Error in class '%s', in method '%s'", owningClass.getName(), name), ex);
        ex.printStackTrace();
    }

    public static void error(Class<?> owningClass, String name, Throwable ex, String message) {
        if (LOGGER == null) {
            System.out.printf("Error in class '%s', in method '%s'%n", owningClass.getName(), name);
            return;
        }
        LOGGER.log(Level.SEVERE, String.format("Error in class '%s', in method '%s' due to '%s'", owningClass.getName(), name, message), ex);
    }

    public static void debugParameters(String msg, List<CommandParameter> parameters) {
        if (LOGGER == null) {
            System.out.printf((msg) + "%n", parameters.stream().map(CommandParameter::format)
                    .collect(Collectors.joining(",")));
            return;
        }
        LOGGER.log(Level.INFO, String.format(msg, parameters.stream().map(CommandParameter::format)
                .collect(Collectors.joining(","))));
    }

}
