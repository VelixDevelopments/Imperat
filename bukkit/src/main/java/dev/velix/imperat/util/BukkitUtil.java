package dev.velix.imperat.util;

import dev.velix.imperat.BukkitImperat;
import dev.velix.imperat.util.reflection.FieldAccessor;
import dev.velix.imperat.util.reflection.Reflections;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.*;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BukkitUtil {

    public static CommandMap COMMAND_MAP;
    public static @Nullable Field KNOWN_COMMANDS;

    static {
        try {
            loadBukkitFieldMappings();
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            ImperatDebugger.error(BukkitImperat.class, "static-init",
                e, "Failed to fetch bukkit command-map, disabling plugin");
        }
    }

    private BukkitUtil() {
    }

    private static void loadBukkitFieldMappings() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        final Class<?> craftServer = Bukkit.getServer().getClass();
        final FieldAccessor<SimpleCommandMap> accessor = Reflections.getField(craftServer, SimpleCommandMap.class);
        COMMAND_MAP = accessor.get(Bukkit.getServer());
        if (COMMAND_MAP != null) {
            KNOWN_COMMANDS = SimpleCommandMap.class.getDeclaredField("knownCommands");
            KNOWN_COMMANDS.setAccessible(true);
        }
    }

    public static final class ClassesRefUtil {
        private static final String SERVER_VERSION = getServerVersion();

        private ClassesRefUtil() {
        }

        private static String getServerVersion() {
            Class<?> server = Bukkit.getServer().getClass();
            if (!server.getSimpleName().equals("CraftServer")) {
                return ".";
            }
            if (server.getName().equals("org.bukkit.craftbukkit.CraftServer")) {
                // Non versioned class
                return ".";
            } else {
                String version = server.getName().substring("org.bukkit.craftbukkit".length());
                return version.substring(0, version.length() - "CraftServer".length());
            }
        }

        public static String mc(String name) {
            return "net.minecraft." + name;
        }

        public static String nms(String className) {
            return "net.minecraft.server" + SERVER_VERSION + className;
        }

        public static Class<?> mcClass(String className) throws ClassNotFoundException {
            return Class.forName(mc(className));
        }

        public static Class<?> nmsClass(String className) throws ClassNotFoundException {
            return Class.forName(nms(className));
        }

        public static String obc(String className) {
            return "org.bukkit.craftbukkit" + SERVER_VERSION + className;
        }

        public static Class<?> obcClass(String className) throws ClassNotFoundException {
            return Class.forName(obc(className));
        }

        public static int minecraftVersion() {
            try {
                final Matcher matcher = Pattern.compile("\\(MC: (\\d)\\.(\\d+)\\.?(\\d+?)?( .*)?\\)").matcher(Bukkit.getVersion());
                if (matcher.find()) {
                    return Integer.parseInt(matcher.toMatchResult().group(2), 10);
                } else {
                    throw new IllegalArgumentException(String.format("No match found in '%s'", Bukkit.getVersion()));
                }
            } catch (final IllegalArgumentException ex) {
                throw new RuntimeException("Failed to determine Minecraft version", ex);
            }
        }

    }

    @SuppressWarnings("unchecked")
    public static Class<? extends Entity> getSelectedEntity(@NotNull Type selectorType) {
        return (Class<? extends Entity>) TypeUtility.getInsideGeneric(selectorType, Entity.class);
    }

    public static <T> Set<T> mergedSet(Set<T> set1, Set<T> set2, Supplier<Set<T>> supplier) {
        Set<T> merged = supplier.get();
        merged.addAll(set1);
        merged.addAll(set2);
        return merged;
    }
}
