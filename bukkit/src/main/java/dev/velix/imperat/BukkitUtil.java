package dev.velix.imperat;

import dev.array21.bukkitreflectionlib.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;

import java.lang.reflect.Field;
import java.util.Map;

class BukkitUtil {

	private BukkitUtil() {

	}

	public static Class<?> getCraftServer() {

		/*if(ReflectionUtil.isUseNewSpigotPackaging()) {
			// >= Minecraft 1.17
			return ReflectionUtil.getMinecraftClass("world.entity.player.EntityHuman");
		} else {
			// =< Minecraft 1.16
			// This method is also marked as @Deprecated !
			return ReflectionUtil.getNmsClass("EntityHuman");
		}*/
		try {
			return ReflectionUtil.getBukkitClass("CraftServer");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	@SuppressWarnings("unchecked")
	public static Map<String, org.bukkit.command.Command> getCommandMap() throws NoSuchFieldException, IllegalAccessException {
		Class<?> craftServer = getCraftServer();
		Field commandMapField = craftServer.getDeclaredField("commandMap");
		commandMapField.setAccessible(true);
		CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
		// Get the commands registered in the command map
		Field hashMapCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
		hashMapCommandsField.setAccessible(true);
		return (Map<String, Command>) hashMapCommandsField.get(commandMap);
	}

}
