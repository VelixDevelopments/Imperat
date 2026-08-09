package studio.mevera.imperat;

/**
 * Lenient parser for server version strings.
 * <p>
 * Servers no longer guarantee a {@code Major.Minor.Patch} scheme; modern builds report things like
 * {@code 26.2.build.46-alpha}. This parser reads the leading numeric segments and ignores whatever
 * follows, so an unexpected scheme degrades gracefully instead of throwing.
 * <p>
 * Deliberately free of any Bukkit reference, so it stays unit-testable on its own.
 */
final class VersionParser {

    /**
     * Used when nothing numeric could be read at all. Treated as "newest", since every version
     * check in this module gates a legacy fallback path.
     */
    static final int[] UNKNOWN = {Integer.MAX_VALUE, 0, 0};

    private VersionParser() {
    }

    /**
     * Strips the build/qualifier suffix, e.g. {@code 1.21.11-R0.1-SNAPSHOT} to {@code 1.21.11}.
     *
     * @param raw the raw version, may be null
     * @return the version without its suffix, never null
     */
    static String stripSuffix(final String raw) {
        if (raw == null) {
            return "";
        }
        final int dash = raw.indexOf('-');
        return dash == -1 ? raw : raw.substring(0, dash);
    }

    /**
     * Parses the leading numeric segments of a version string.
     *
     * @param raw the raw version, may be null
     * @return exactly three elements: major, minor, patch. Never null, never throws.
     */
    static int[] parse(final String raw) {
        final String version = stripSuffix(raw);
        if (version.isEmpty()) {
            return UNKNOWN.clone();
        }

        final String[] segments = version.split("\\.");
        final int[] parsed = {0, 0, 0};
        int count = 0;

        for (final String segment : segments) {
            if (count == parsed.length) {
                break;
            }
            final int value = leadingNumber(segment);
            if (value < 0) {
                // first non-numeric segment ends the version, e.g. the "build" in "26.2.build.46"
                break;
            }
            parsed[count++] = value;
        }

        return count == 0 ? UNKNOWN.clone() : parsed;
    }

    /**
     * Reads the digits at the start of a segment, tolerating a trailing tail such as the
     * {@code b} in {@code 1.21.4b}.
     *
     * @return the parsed number, or {@code -1} if the segment doesn't start with a digit
     * or overflows an int
     */
    private static int leadingNumber(final String segment) {
        int end = 0;
        while (end < segment.length() && Character.isDigit(segment.charAt(end))) {
            end++;
        }
        if (end == 0) {
            return -1;
        }
        try {
            return Integer.parseInt(segment, 0, end, 10);
        } catch (final NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Compares a parsed version against the given components.
     *
     * @param version a three-element array as returned by {@link #parse(String)}
     * @return a negative number if {@code version} is older, zero if equal, positive if newer
     */
    static int compare(final int[] version, final int major, final int minor, final int patch) {
        if (version[0] != major) {
            return Integer.compare(version[0], major);
        }
        if (version[1] != minor) {
            return Integer.compare(version[1], minor);
        }
        return Integer.compare(version[2], patch);
    }

}
