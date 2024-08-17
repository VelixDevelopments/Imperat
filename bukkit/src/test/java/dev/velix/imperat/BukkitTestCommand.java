package dev.velix.imperat;


import dev.velix.imperat.test.Group;
import dev.velix.imperat.test.GroupRegistry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.List;

public class BukkitTestCommand implements TabExecutor {
	
	@Override
	public boolean onCommand(CommandSender commandSender,
	                         Command command, String label, String[] args) {
		
		if (args.length == 0) {
			commandSender.sendMessage("/group help");
			return false;
		}
		
		//group <group>
		Group group = GroupRegistry.getInstance().getData(args[0]).orElse(null);
		if (group == null) {
			commandSender.sendMessage("UNKNOWN GROUP !");
			return false;
		}
		// /group <group> setperm <permission>
		// /group <group> setprefix <prefix>
		
		if (args.length == 3) {
			String value = args[2];
			switch (args[1]) {
				case "setperm":
					System.out.println("Setting permission to " + value);
					break;
				case "setprefix":
					System.out.println("Setting prefix to " + value);
					break;
				default:
					System.out.println("Unknown syntax !");
					break;
			}
		}
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
		List<String> arrayList = new ArrayList<>();
		if (args.length == 0 || args.length == 1) {
			for (Group group : GroupRegistry.getInstance().getAll()) {
				arrayList.add(group.getName());
			}
			return arrayList;
		}
		
		if (args.length == 2) {
			arrayList.add("setprefix");
			arrayList.add("setperm");
			return arrayList;
		}
		
		return arrayList;
	}
	
}
