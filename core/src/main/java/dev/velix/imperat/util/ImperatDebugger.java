package dev.velix.imperat.util;

import dev.velix.imperat.command.parameters.CommandParameter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class ImperatDebugger {

    private static @NotNull Logger LOGGER = Logger.getLogger("IMPERAT");
    private static boolean enabled = false;

    private ImperatDebugger() {
    }

    public static void setEnabled(boolean enabled) {
        ImperatDebugger.enabled = enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setLogger(Logger LOGGER) {
        ImperatDebugger.LOGGER = LOGGER;
    }

    public static void debug(String msg, Object... args) {
        if (!enabled) return;
        LOGGER.log(Level.INFO, String.format(msg, args));
    }

    public static void warning(String msg, Object... args) {
        if (!enabled) return;
        LOGGER.log(Level.WARNING, String.format(msg, args));
    }

    public static void error(Class<?> owningClass, String name, @NotNull Throwable ex) {
        if (!enabled) return;
        LOGGER.log(Level.SEVERE, String.format("Error in class '%s', in method '%s'", owningClass.getName(), name), ex);
    }

    public static void error(Class<?> owningClass, String name, Throwable ex, String message) {
        if (!enabled) return;
        LOGGER.log(Level.SEVERE, String.format("Error in class '%s', in method '%s' due to '%s'", owningClass.getName(), name, message), ex);
    }

    public static void debugParameters(String msg, List<CommandParameter<?>> parameters) {
        if (!enabled) return;
        LOGGER.log(Level.INFO, String.format(msg, parameters.stream().map(CommandParameter::format)
            .collect(Collectors.joining(","))));
    }

}
