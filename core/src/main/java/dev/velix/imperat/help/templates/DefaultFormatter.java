package dev.velix.imperat.help.templates;

import dev.velix.imperat.CommandDispatcher;
import dev.velix.imperat.caption.Messages;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.help.PlainDisplayer;
import dev.velix.imperat.help.TreeDisplayer;
import dev.velix.imperat.help.UsageFormatter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class DefaultFormatter implements UsageFormatter {


	/**
	 * Displays the usage by converting it into
	 * an adventure component
	 * <p>
	 * This is used in the {@link PlainDisplayer}
	 *
	 * @param dispatcher the dispatcher
	 * @param command    the command
	 * @param usage      the usage to display
	 * @param isLast     is it the last usage
	 * @return the usage component
	 */
	@Override
	public <C> Component formatUsage(@NotNull CommandDispatcher<C> dispatcher, Command<C> command, CommandUsage<C> usage, boolean isLast) {
		String format = dispatcher.commandPrefix() + CommandUsage.format(command, usage);
		String msg = "<dark_gray><bold>[<dark_aqua>+</dark_aqua>]</bold></dark_gray><green>" + format + " <white><bold>-</bold></white> <yellow>" + usage.getDescription();
		return Messages.getMsg(msg);
	}

	/**
	 * Formats a single syntax, this is used
	 * only in the {@link TreeDisplayer}
	 *
	 * @param formattedUsage the format of the usage syntax
	 * @return the format component of the usage
	 */
	@Override
	public Component formatUsage(String formattedUsage) {
		String msg = "<green>" + formattedUsage;
		return Messages.getMsg(msg);
	}

}
