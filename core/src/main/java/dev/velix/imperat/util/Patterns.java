package dev.velix.imperat.util;

import java.util.regex.Pattern;

public final class Patterns {

    public final static Pattern SINGLE_FLAG = Pattern.compile("-([a-zA-Z]+)");
    public final static Pattern DOUBLE_FLAG = Pattern.compile("--([a-zA-Z]+)");

    private Patterns() {
        throw new AssertionError();
    }

    public static boolean isSingleFlag(String input) {
        return SINGLE_FLAG.matcher(input).matches();
    }

    public static boolean isDoubleFlag(String input) {
        return DOUBLE_FLAG.matcher(input).matches();
    }

    public static boolean isInputFlag(String input) {
        return isSingleFlag(input) || isDoubleFlag(input);
    }


}
