package dev.velix.imperat.examples;


import dev.velix.imperat.BukkitCommandSource;
import dev.velix.imperat.annotations.types.Command;
import dev.velix.imperat.annotations.types.Description;
import dev.velix.imperat.annotations.types.Permission;
import dev.velix.imperat.annotations.types.methods.DefaultUsage;
import dev.velix.imperat.annotations.types.methods.SubCommand;
import dev.velix.imperat.annotations.types.methods.Usage;
import dev.velix.imperat.annotations.types.parameters.Arg;
import dev.velix.imperat.test.Group;

@Command({"group", "rank"})
@Permission("command.group")
@Description("Main command for managing groups/ranks")
public final class GroupCommand {

	@DefaultUsage
	public void defaultUsage(BukkitCommandSource source) {
		source.reply("/group help");
	}

	@Usage
	public void group(BukkitCommandSource source, @Arg("group") Group group) {
		source.reply("hello , group entered= " + group.getName());
	}

	@SubCommand(value = "setperm")
	@Permission("command.group.setperm")
	public void setPermission(BukkitCommandSource source,
	                          @Arg("group") Group group,
	                          @Arg("permission") String permission) {
		source.reply("You have set permission '" + permission + "' to group '" + group.getName() + "'");
	}

	@SubCommand("setprefix")
	@Permission("command.group.setprefix")
	public void setPrefix(
			  BukkitCommandSource source,
			  @Arg("group") Group group,
			  @Arg("prefix") String prefix
	) {
		source.reply("You have set prefix '" + prefix + "' to group '" + group.getName() + "'");
	}


}
