package dev.velix.imperat.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringUtils {
	/**
	 * General utilities for string operations
	 */

	private StringUtils() {
	}

	/**
	 * Pattern to split by whitespace
	 */
	public static final Pattern SPACE = Pattern.compile(" ", Pattern.LITERAL);

	/**
	 * Pattern to split by whitespace
	 */
	@SuppressWarnings("RegExpEmptyAlternationBranch") // we have LITERAL :face_palm:
	public static final Pattern VERTICAL_BAR = Pattern.compile("|", Pattern.LITERAL);

	/**
	 * Pattern to extract snowflake IDs. Useful for JDA
	 */
	public static final Pattern SNOWFLAKE = Pattern.compile("<(@!|@|@&|#)(?<snowflake>\\d{18})>");

	public static LinkedList<String> splitBySpace(String text) {
		String[] result = SPACE.split(text);
		LinkedList<String> list = new LinkedList<>();
		Collections.addAll(list, result);
		return list;
	}

	public static @Nullable String getSnowflake(String mention) {
		Matcher matcher = SNOWFLAKE.matcher(mention);
		if (matcher.find())
			return matcher.group(2);
		return null;
	}


	public static @NotNull String stripNamespace(String namespace, @NotNull String command) {
		int colon = command.indexOf(namespace + ':');
		if (colon == -1) {
			return command;
		}
		// +1 for the ':'
		return command.substring(namespace.length() + 1);
	}

	public static @NotNull String stripNamespace(@NotNull String command) {
		int colon = command.indexOf(':');
		if (colon == -1)
			return command;
		return command.substring(colon + 1);
	}

	public static String repeat(String string, int count) {
		Preconditions.notNull(string, "string");

		if (count <= 1) {
			Preconditions.checkArgument(count >= 0, "invalid count: " + count);
			return (count == 0) ? "" : string;
		}

		final int len = string.length();
		final long longSize = (long) len * (long) count;
		final int size = (int) longSize;
		if (size != longSize) {
			throw new ArrayIndexOutOfBoundsException("Required array size too large: " + longSize);
		}

		final char[] array = new char[size];
		string.getChars(0, len, array, 0);
		int n;
		for (n = len; n < size - n; n <<= 1) {
			System.arraycopy(array, 0, array, n, n);
		}
		System.arraycopy(array, 0, array, n, size - n);
		return new String(array);
	}

	/*public static String colorize(@NotNull String text) {
		char[] b = text.toCharArray();
		for (int i = 0; i < b.length - 1; i++) {
			if (b[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(b[i + 1]) > -1) {
				b[i] = '§';
				b[i + 1] = Character.toLowerCase(b[i + 1]);
			}
		}
		return new String(b);
	}*/

	public static String normalizedParameterFormatting(String parameterContent, boolean optional) {
		String prefix, suffix;
		if(optional) {
			prefix="[";
			suffix = "]";
		}else {
			prefix="<";
			suffix=">";
		}
		return prefix + parameterContent + suffix;
	}
}

