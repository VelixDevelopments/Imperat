package dev.velix.imperat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;

import java.lang.reflect.Field;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;

class BukkitUtil {

	private BukkitUtil() {

	}


	@SuppressWarnings("unchecked")
	public static Map<String, org.bukkit.command.Command> getCommandMap() throws NoSuchFieldException, IllegalAccessException {
		CraftServer craftServer = (CraftServer) getServer();
		Field commandMapField = craftServer.getClass().getDeclaredField("commandMap");
		commandMapField.setAccessible(true);
		CommandMap commandMap = (CommandMap) commandMapField.get(craftServer);

		// Get the commands registered in the command map
		Field hashMapCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
		hashMapCommandsField.setAccessible(true);
		return (Map<String, Command>) hashMapCommandsField.get(commandMap);
	}

}
