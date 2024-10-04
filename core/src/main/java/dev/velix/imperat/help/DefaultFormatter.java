package dev.velix.imperat.help;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
final class DefaultFormatter implements UsageFormatter {
	
	final static DefaultFormatter INSTANCE = new DefaultFormatter();
	
	private DefaultFormatter() {
	
	}
	
	@Override
	public <S extends Source> String format(Command<S> command, CommandUsage<S> usage, int index) {
		String format = "/" + CommandUsage.format(command, usage);
		return "&8&l[&3+&8]&r &a" + format + " &r&l-&r&e " + usage.description();
	}
	
	
}
