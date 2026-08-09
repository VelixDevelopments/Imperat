package studio.mevera.imperat;

import org.bukkit.Bukkit;
import studio.mevera.imperat.util.reflection.Reflections;

/**
 * The running server's version.
 * <p>
 * Servers no longer guarantee a {@code Major.Minor.Patch} scheme (e.g. Leaf reports
 * {@code 26.2.build.46-alpha}), so parsing is delegated to the lenient {@link VersionParser}.
 * Unparsable versions are treated as the newest possible version, since every check here gates a
 * legacy fallback.
 */
public final class Version {

    public static final String VERSION_EXACT = VersionParser.stripSuffix(Bukkit.getBukkitVersion());
    public static final boolean IS_FOLIA = Reflections.findClass("io.papermc.paper.threadedregions.RegionizedServer");
    public static final boolean IS_PAPER =
            Reflections.findClass("com.destroystokyo.paper.PaperConfig", "io.papermc.paper.configuration.Configuration");
    public static final boolean SUPPORTS_PAPER_ASYNC_TAB_COMPLETION =
            Reflections.findClass("com.destroystokyo.paper.event.server.AsyncTabCompleteEvent");
    public static final int MAJOR, MINOR, PATCH;
    // initialize after IS_PAPER is initialized
    //public static final String NMS = findVersion();

    static {
        final int[] versions = VersionParser.parse(Bukkit.getBukkitVersion());
        MAJOR = versions[0];
        MINOR = versions[1];
        PATCH = versions[2];
    }

    private static int compare(final int major, final int minor, final int patch) {
        return VersionParser.compare(new int[]{MAJOR, MINOR, PATCH}, major, minor, patch);
    }

    public static boolean is(final int major, final int minor, final int patch) {
        return compare(major, minor, patch) == 0;
    }

    public static boolean isOver(final int major, final int minor, final int patch) {
        return compare(major, minor, patch) > 0;
    }

    public static boolean isOrOver(final int major, final int minor, final int patch) {
        return compare(major, minor, patch) >= 0;
    }

    public static boolean isBelow(final int major, final int minor, final int patch) {
        return compare(major, minor, patch) < 0;
    }

    public static boolean isOrBelow(final int major, final int minor, final int patch) {
        return compare(major, minor, patch) <= 0;
    }

}
