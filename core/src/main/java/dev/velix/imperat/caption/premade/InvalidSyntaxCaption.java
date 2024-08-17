package dev.velix.imperat.caption.premade;

import dev.velix.imperat.CommandDispatcher;
import dev.velix.imperat.CommandSource;
import dev.velix.imperat.caption.Caption;
import dev.velix.imperat.caption.CaptionKey;
import dev.velix.imperat.caption.Messages;
import dev.velix.imperat.command.BaseCommandDispatcher;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.UsageParameter;
import dev.velix.imperat.context.Context;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class InvalidSyntaxCaption<C> implements Caption<C> {
	/**
	 * @return the key
	 */
	@Override
	public @NotNull CaptionKey getKey() {
		return CaptionKey.INVALID_SYNTAX;
	}
	
	/**
	 * @param dispatcher    the dispatcher
	 * @param command       the command
	 * @param commandSource the source
	 * @param context       the context
	 * @param usage         the command usage, can be null if it hasn't been resolved yet
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
		
		if (usage == null) {
			//UNKNOWN USAGE
			return Messages.getMsg(Messages.INVALID_SYNTAX_UNKNOWN_USAGE,
							Placeholder.parsed("raw_args", context.getArguments().join(" ")));
		} else {
			final int last = context.getArguments().size() - 1;
			
			List<UsageParameter> params = new ArrayList<>(usage.getParameters())
							.stream()
							.filter((param) -> !param.isOptional() && param.getPosition() > last)
							.toList();
			
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < params.size(); i++) {
				UsageParameter param = params.get(i);
				assert !param.isOptional();
				builder.append(param.format(command));
				if (i != params.size() - 1)
					builder.append(' ');
				
			}
			//INCOMPLETE USAGE, AKA MISSING REQUIRED INPUTS
			return Messages.getMsg(Messages.INVALID_SYNTAX_INCOMPLETE_USAGE,
											Placeholder.parsed("required_args", builder.toString()))
							.appendNewline()
							.append(BaseCommandDispatcher.FULL_SYNTAX_PREFIX).append(
											Messages.getMsg(Messages.INVALID_SYNTAX_ORIGINAL_USAGE_SHOWCASE, Placeholder.parsed("usage", dispatcher.commandPrefix() + CommandUsage.format(command, usage)))
							);
		}
	}
}
