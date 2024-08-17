package dev.velix.imperat.help;

import dev.velix.imperat.CommandDispatcher;
import dev.velix.imperat.CommandSource;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Internal
public final class PlainDisplayer implements UsagesDisplayer {
	
	PlainDisplayer() {
	
	}
	
	@Override
	public <C> void display(CommandDispatcher<C> dispatcher,
	                        Command<C> command,
	                        CommandSource<C> source,
	                        UsageFormatter formatter,
	                        List<CommandUsage<C>> commandUsages) {
		
		for (int i = 0; i < commandUsages.size(); i++) {
			var usage = commandUsages.get(i);
			var comp = formatter.formatUsage(dispatcher, command, usage, i == commandUsages.size() - 1);
			source.reply(comp);
		}
	}
	
}
