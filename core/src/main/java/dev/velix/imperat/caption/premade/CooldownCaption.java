package dev.velix.imperat.caption.premade;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.CommandDispatcher;
import dev.velix.imperat.CommandSource;
import dev.velix.imperat.caption.Caption;
import dev.velix.imperat.caption.CaptionKey;
import dev.velix.imperat.caption.Messages;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.cooldown.CooldownHandler;
import dev.velix.imperat.command.cooldown.UsageCooldown;
import dev.velix.imperat.context.Context;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.concurrent.TimeUnit;

public final class CooldownCaption<C> implements Caption<C> {

	/**
	 * @return the key
	 */
	@Override
	public @NotNull CaptionKey getKey() {
		return CaptionKey.COOLDOWN;
	}

	/**
	 * @param dispatcher the dispatcher
	 * @param command  the command
	 * @param usage the usage
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
		if (usage == null || usage.getCooldown() == null) {
			return Component.empty();
		}
		return Messages.getMsg(Messages.COOL_DOWN_WAIT,
				  Placeholder.parsed("time", formatTime(commandSource, usage)));
	}

	private String formatTime(CommandSource<C> source, CommandUsage<C> usage) {
		return formatTime(source, usage.getCooldown(), usage.getCooldownHandler());
	}

	private String formatTime(CommandSource<C> source,
	                          UsageCooldown cooldown,
	                          CooldownHandler<C> cooldownHandler) {
		return formatTime(cooldown, cooldownHandler.getLastTimeExecuted(source).orElse(-1L));
	}

	private String formatTime(UsageCooldown cooldown, long lastTimeExecuted) {
		long timePassed = System.currentTimeMillis()-lastTimeExecuted;
		long remaining = cooldown.toMillis()-timePassed;
		return TimeUnit.MILLISECONDS.toSeconds(remaining) + " second(s) ";
	}
}
