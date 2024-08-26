package dev.velix.imperat;

import dev.array21.bukkitreflectionlib.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

class BukkitUtil {

    private BukkitUtil() {

    }
    
    public static CommandMap COMMAND_MAP;
    public static @Nullable Field KNOWN_COMMANDS;
    
    static {
	    try {
		    loadBukkitFieldMappings();
	    } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
		    CommandDebugger.warning("Failed to fetch bukkit command-map, disabling plugin");
		    e.printStackTrace();
	    }
    }
    
    public static void loadBukkitFieldMappings() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Class<?> craftServer = getCraftServer();
        Field commandMapField = craftServer.getDeclaredField("commandMap");
        commandMapField.setAccessible(true);
        COMMAND_MAP = (CommandMap) commandMapField.get(Bukkit.getServer());
        
        if(COMMAND_MAP instanceof SimpleCommandMap) {
            KNOWN_COMMANDS = SimpleCommandMap.class.getDeclaredField("knownCommands");
            KNOWN_COMMANDS.setAccessible(true);
        }
    }
    

    public static Class<?> getCraftServer() throws ClassNotFoundException {

		/*if(ReflectionUtil.isUseNewSpigotPackaging()) {
			// >= Minecraft 1.17
			return ReflectionUtil.getMinecraftClass("world.entity.player.EntityHuman");
		} else {
			// =< Minecraft 1.16
			// This method is also marked as @Deprecated !
			return ReflectionUtil.getNmsClass("EntityHuman");
		}*/
        return ReflectionUtil.getBukkitClass("CraftServer");
    }

}
