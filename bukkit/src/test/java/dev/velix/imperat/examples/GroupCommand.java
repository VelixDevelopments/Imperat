package dev.velix.imperat.examples;


import dev.velix.imperat.BukkitCommandHelp;
import dev.velix.imperat.BukkitCommandSource;
import dev.velix.imperat.annotations.types.Command;
import dev.velix.imperat.annotations.types.Description;
import dev.velix.imperat.annotations.types.Permission;
import dev.velix.imperat.annotations.types.methods.DefaultUsage;
import dev.velix.imperat.annotations.types.methods.Help;
import dev.velix.imperat.annotations.types.methods.SubCommand;
import dev.velix.imperat.annotations.types.methods.Usage;
import dev.velix.imperat.annotations.types.parameters.Named;
import dev.velix.imperat.test.Group;

@Command({"group", "rank"})
@Permission("command.group")
@Description("Main command for managing groups/ranks")
public final class GroupCommand {
	
	@DefaultUsage
	public void defaultUsage(BukkitCommandSource source) {
		//default execution = no args
		source.reply("/group <group>");
	}
	
	@Usage
	public void mainUsage(BukkitCommandSource source, @Named("group") Group group) {
		//when he does "/group <group>"
		source.reply("entered group name= " + group.getName());
	}
	
	@Help
	public void groupHelp(
					BukkitCommandSource source,
					@Named("group") Group group,
					BukkitCommandHelp help
	) {
		System.out.println("INSIDE METHOD HELP");
		source.reply("Group entered= " + group.getName());
		help.display(source);
	}
	
	@SubCommand(value = "setperm")
	@Permission("command.group.setperm")
	public void setPermission(BukkitCommandSource source,
	                          @Named("group") Group group,
	                          @Named("permission") String permission) {
		// /group <group> setperm <permission>
		source.reply("You have set permission '" + permission
						+ "' to group '" + group.getName() + "'");
	}
	
	@SubCommand("setprefix")
	@Permission("command.group.setprefix")
	public void setPrefix(
					BukkitCommandSource source,
					@Named("group") Group group,
					@Named("prefix") String prefix
	) {
		// /group <group> setprefix <prefix>
		source.reply("You have set prefix '" + prefix + "' to group '" + group.getName() + "'");
	}

	/*@SubCommand(value = "info")
	public void info(BukkitCommandSource source, Group group) {
		if(source.isConsole()) {
			source.reply("You can't do that, only players can !");
			return;
		}
		source.reply("Showing info about group: " + group.getName());
	}*/
	
}
