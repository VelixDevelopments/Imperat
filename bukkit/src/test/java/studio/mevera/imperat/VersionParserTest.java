package studio.mevera.imperat;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Covers the lenient version parsing that backs {@link Version}. {@link VersionParser} is tested
 * directly because touching {@code Version} would require a live server for its static initializer.
 */
class VersionParserTest {

    // --- backwards compatibility: classic Major.Minor.Patch must parse exactly as before ---

    @Test
    void parsesLegacyVersions() {
        assertArrayEquals(new int[]{1, 8, 8}, VersionParser.parse("1.8.8"));
        assertArrayEquals(new int[]{1, 12, 2}, VersionParser.parse("1.12.2-R0.1-SNAPSHOT"));
        assertArrayEquals(new int[]{1, 16, 5}, VersionParser.parse("1.16.5-R0.1-SNAPSHOT"));
        assertArrayEquals(new int[]{1, 21, 11}, VersionParser.parse("1.21.11-R0.1-SNAPSHOT"));
    }

    @Test
    void treatsMissingPatchAsZero() {
        assertArrayEquals(new int[]{1, 13, 0}, VersionParser.parse("1.13-R0.1-SNAPSHOT"));
        assertArrayEquals(new int[]{26, 2, 0}, VersionParser.parse("26.2"));
    }

    // --- the new schemes that used to blow up ---

    @Test
    void parsesLeafStyleBuildVersion() {
        assertArrayEquals(new int[]{26, 2, 0}, VersionParser.parse("26.2.build.46-alpha"));
    }

    @Test
    void toleratesNonDigitTail() {
        assertArrayEquals(new int[]{1, 21, 4}, VersionParser.parse("1.21.4b"));
    }

    @Test
    void fallsBackToNewestWhenUnparsable() {
        assertArrayEquals(VersionParser.UNKNOWN, VersionParser.parse(null));
        assertArrayEquals(VersionParser.UNKNOWN, VersionParser.parse(""));
        assertArrayEquals(VersionParser.UNKNOWN, VersionParser.parse("weird"));
        assertArrayEquals(VersionParser.UNKNOWN, VersionParser.parse("-alpha"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "-", ".", "..", "weird", "1.", "1..2", "99999999999.1.1", "1.21.11-R0.1-SNAPSHOT"})
    void neverThrows(final String raw) {
        assertDoesNotThrow(() -> VersionParser.parse(raw));
    }

    @Test
    void stripsSuffix() {
        assertEquals("1.21.11", VersionParser.stripSuffix("1.21.11-R0.1-SNAPSHOT"));
        assertEquals("26.2.build.46", VersionParser.stripSuffix("26.2.build.46-alpha"));
        assertEquals("", VersionParser.stripSuffix(null));
    }

    // --- the comparisons the call sites actually rely on ---

    @Test
    void legacyGatesStillFlipAtTheSameBoundaries() {
        // Version.isOrOver(1, 13, 0) — used by BukkitImperat and TargetSelectorArgument
        assertFalse(isOrOver("1.12.2-R0.1-SNAPSHOT", 1, 13, 0));
        assertTrue(isOrOver("1.13-R0.1-SNAPSHOT", 1, 13, 0));
        assertTrue(isOrOver("1.21.11-R0.1-SNAPSHOT", 1, 13, 0));

        // Version.isOver(1, 16, 5) — used by the brigadier/commodore paths
        assertFalse(isOver("1.16.5-R0.1-SNAPSHOT", 1, 16, 5));
        assertTrue(isOver("1.16.6", 1, 16, 5));
    }

    @Test
    void leafVersionTakesTheModernBranches() {
        final String leaf = "26.2.build.46-alpha";
        assertEquals(26, VersionParser.parse(leaf)[0]);
        assertTrue(isOrOver(leaf, 1, 13, 0));
        assertFalse(isOrBelow(leaf, 1, 13, 0));
        assertTrue(isOver(leaf, 1, 16, 5));
    }

    @Test
    void unknownVersionTakesTheModernBranches() {
        assertTrue(isOrOver("weird", 1, 13, 0));
        assertFalse(isOrBelow("weird", 1, 13, 0));
    }

    private static boolean isOver(final String raw, final int major, final int minor, final int patch) {
        return VersionParser.compare(VersionParser.parse(raw), major, minor, patch) > 0;
    }

    private static boolean isOrOver(final String raw, final int major, final int minor, final int patch) {
        return VersionParser.compare(VersionParser.parse(raw), major, minor, patch) >= 0;
    }

    private static boolean isOrBelow(final String raw, final int major, final int minor, final int patch) {
        return VersionParser.compare(VersionParser.parse(raw), major, minor, patch) <= 0;
    }

}
