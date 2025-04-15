package dev.velix.imperat.util;

import dev.velix.imperat.context.ArgumentQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringUtils {
    public final static char DOUBLE_QUOTE = '"', SINGLE_QUOTE = '\'';

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
            return !autoCompletion ? ArgumentQueue.of(argumentsInOneLine) : ArgumentQueue.parse(" ");

        ArgumentQueue toCollect = ArgumentQueue.of(argumentsInOneLine);
        char[] chars = argumentsInOneLine.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            if (isQuoteChar(c) && i != chars.length - 1) {
                int start = i + 1;
                while (start < chars.length && !isEndOfQuote(c, chars[start])) {
                    builder.append(chars[start]);
                    start++;
                }
                i = start;

                toCollect.add(builder.toString());
                builder = new StringBuilder();
                continue;
            }

            if (Character.isWhitespace(c)) {
                toCollect.add(builder.toString());
                builder = new StringBuilder();
                continue;
            }

            builder.append(c);

        }
        if (!builder.isEmpty()) {
            toCollect.add(builder.toString());
        }

        if (autoCompletion && extraSpace)
            toCollect.add(" ");

        return toCollect;
    }

    public static ArgumentQueue parseToQueue(String argumentsInOneLine, boolean autoCompletion) {
        return parseToQueue(argumentsInOneLine, autoCompletion, false);
    }


    public static boolean isQuoteChar(char ch) {
        return ch == DOUBLE_QUOTE || ch == SINGLE_QUOTE;
    }

    public static boolean isEndOfQuote(char quoteStarter, char value) {
        return isQuoteChar(value) && isQuoteChar(quoteStarter) && quoteStarter == value;
    }
}

