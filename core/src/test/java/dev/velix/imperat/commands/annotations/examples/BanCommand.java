package dev.velix.imperat.commands.annotations.examples;

import dev.velix.imperat.TestSource;
import dev.velix.imperat.annotations.*;
import jdk.jfr.Description;
import org.jetbrains.annotations.Nullable;

@Command("ban")
@Permission("command.ban")
@Description("Main command for banning players")
public final class BanCommand {
	
	@Usage
	public void showUsage(TestSource source) {
		source.reply("/ban <player> [-silent] [duration] [reason...]");
	}
	
	@Usage
	public void banPlayer(
		TestSource source,
		@Named("player") String player,
		@Switch({"silent", "s"}) boolean silent,
		@Named("duration") @Optional @Nullable String duration,
		@Named("reason") @Optional @Default("Breaking server laws") @Greedy String reason
	) {
		//TODO actual ban logic
		String durationFormat = duration == null ? "FOREVER" : "for " + duration;
		String msg = "Banning " + player + " " + durationFormat + " due to " + reason;
		if (!silent)
			source.reply("NOT SILENT= " + msg);
		else
			source.reply("SILENT= " + msg);
	}
	
}