package dev.velix.imperat.examples;

import dev.velix.imperat.BukkitCommandSource;
import dev.velix.imperat.annotations.types.Command;
import dev.velix.imperat.annotations.types.methods.DefaultUsage;
import dev.velix.imperat.annotations.types.methods.Usage;
import dev.velix.imperat.annotations.types.parameters.Named;

@Command("example")
public final class ExampleCommand {
	
	@DefaultUsage
	public void defaultUsage(BukkitCommandSource source) {
		source.reply("This is just an example with no arguments entered");
	}
	
	@Usage
	public void exampleOneArg(BukkitCommandSource source, @Named("firstArg") int firstArg) {
		source.reply("Entered required num= " + firstArg);
	}
	
}
