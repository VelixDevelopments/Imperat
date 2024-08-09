package dev.velix.imperat.examples;


import dev.velix.imperat.BukkitCommandSource;
import dev.velix.imperat.annotations.types.Command;
import dev.velix.imperat.annotations.types.Description;
import dev.velix.imperat.annotations.types.Permission;
import dev.velix.imperat.annotations.types.methods.DefaultUsage;
import dev.velix.imperat.annotations.types.methods.SubCommand;
import dev.velix.imperat.annotations.types.parameters.Named;
import dev.velix.imperat.test.Group;

@Command({"group", "rank"})
@Permission("command.group")
@Description("Main command for managing groups/ranks")
public final class GroupCommand {

	@DefaultUsage
	public void defaultUsage(BukkitCommandSource source) {
		source.reply("/group <group>");
	}


	/*@Usage
	@Help
	public void group(BukkitCommandSource source,
	                  @Named("group") Group group,
	                  CommandHelp<CommandSender> help) {
		source.reply("Group entered= " + group.getName());
		help.display(source);
	}*/

	@SubCommand(value = "setperm")
	@Permission("command.group.setperm")
	public void setPermission(BukkitCommandSource source,
	                          @Named("group") Group group,
	                          @Named("permission") String permission) {
		source.reply("You have set permission '" + permission + "' to group '" + group.getName() + "'");
	}

	@SubCommand("setprefix")
	@Permission("command.group.setprefix")
	public void setPrefix(
			  BukkitCommandSource source,
			  @Named("group") Group group,
			  @Named("prefix") String prefix
	) {
		source.reply("You have set prefix '" + prefix + "' to group '" + group.getName() + "'");
	}

	@SubCommand(value = "info")
	public void info(BukkitCommandSource source, Group group) {
		if(source.isConsole()) {
			source.reply("You can't do that, only players can !");
			return;
		}
		source.reply("Showing info about group: " + group.getName());
	}

}
