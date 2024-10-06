package dev.velix.imperat.util;

import dev.velix.imperat.context.ArgumentQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringUtils {

    /**
     * Pattern to extract snowflake IDs. Useful for JDA
     */
    public static final Pattern SNOWFLAKE = Pattern.compile("<(@!|@|@&|#)(?<snowflake>\\d{18})>");

    /**
     * General utilities for string operations
     */
    private StringUtils() {
    }

    public static @Nullable String getSnowflake(String mention) {
        Matcher matcher = SNOWFLAKE.matcher(mention);
        if (matcher.find())
            return matcher.group(2);
        return null;
    }

    public static @NotNull String stripNamespace(@NotNull String command) {
        int colon = command.indexOf(':');
        if (colon == -1)
            return command;
        return command.substring(colon + 1);
    }

    public static String normalizedParameterFormatting(String parameterContent, boolean optional) {
        String prefix, suffix;
        if (optional) {
            prefix = "[";
            suffix = "]";
        } else {
            prefix = "<";
            suffix = ">";
        }
        return prefix + parameterContent + suffix;
    }

    public static ArgumentQueue parseToQueue(String argumentsInOneLine, boolean autoCompletion, boolean extraSpace) {
        if (argumentsInOneLine.isEmpty())
            return !autoCompletion ? ArgumentQueue.empty() : ArgumentQueue.parse(" ");

        ArgumentQueue toCollect = ArgumentQueue.empty();
        toCollect.addAll(Arrays.asList(argumentsInOneLine.split(" ")));

        if (autoCompletion && extraSpace)
            toCollect.add(" ");

        return toCollect;
    }

    public static ArgumentQueue parseToQueue(String argumentsInOneLine, boolean autoCompletion) {
        return parseToQueue(argumentsInOneLine, autoCompletion, false);
    }
}

