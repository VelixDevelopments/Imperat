package dev.velix.imperat.help;

import dev.velix.imperat.CommandSource;
import dev.velix.imperat.command.Command;
import org.jetbrains.annotations.NotNull;

public abstract class CommandHelpWriter<C> {

	protected final HelpTemplate template;

	public CommandHelpWriter(HelpTemplate template) {
		this.template = template;
	}


	public abstract void writeHelp(
			  @NotNull Command<C> main,
			  @NotNull Command<C> target,
			  CommandSource<C> commandSource
	);

}
