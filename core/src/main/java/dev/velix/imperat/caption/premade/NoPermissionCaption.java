package dev.velix.imperat.caption.premade;

import dev.velix.imperat.CommandDispatcher;
import dev.velix.imperat.CommandSource;
import dev.velix.imperat.caption.Caption;
import dev.velix.imperat.caption.CaptionKey;
import dev.velix.imperat.caption.Messages;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.context.Context;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.velix.imperat.caption.Messages.getMsg;

public final class NoPermissionCaption<C> implements Caption<C> {
	
	/**
	 * @return the key
	 */
	@Override
	public @NotNull CaptionKey getKey() {
		return CaptionKey.NO_PERMISSION;
	}
	
	/**
	 * @param dispatcher    the dispatcher
	 * @param commandSource the source
	 * @param context       the context
	 * @param exception     the exception, may be null if no exception provided
	 * @return The message in the form of a component
	 */
	@Override
	public @NotNull Component asComponent(
					@NotNull CommandDispatcher<C> dispatcher,
					@NotNull Command<C> command,
					@NotNull CommandSource<C> commandSource,
					@NotNull Context<C> context,
					@Nullable CommandUsage<C> usage,
					@Nullable Exception exception
	) {
		return getMsg(Messages.NO_PERMISSION);
	}
}
