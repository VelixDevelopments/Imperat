package dev.velix.imperat.examples;

import dev.velix.imperat.BukkitCommandSource;
import dev.velix.imperat.annotations.types.Command;
import dev.velix.imperat.annotations.types.Description;
import dev.velix.imperat.annotations.types.Permission;
import dev.velix.imperat.annotations.types.methods.DefaultUsage;
import dev.velix.imperat.annotations.types.methods.Usage;
import dev.velix.imperat.annotations.types.parameters.Greedy;
import dev.velix.imperat.annotations.types.parameters.Named;
import org.bukkit.Bukkit;

@Command("broadcast")
@Permission("command.broadcast")
@Description("A broadcast command")
public final class BroadcastCommand {
	
	
	@DefaultUsage //usage without args
	public void example(BukkitCommandSource source) {
		source.reply("/broadcast <message...>");
	}
	
	
	@Usage
	@Description("broadcasts a message")
	public void example(BukkitCommandSource source, @Named("message") @Greedy String msg) {
		// /broadcast <msg...>
		for (var player : Bukkit.getOnlinePlayers())
			player.sendMessage(msg);
		
		source.reply("Broadcast has been sent !");
	}
	
}
