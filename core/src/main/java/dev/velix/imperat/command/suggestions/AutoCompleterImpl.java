package dev.velix.imperat.command.suggestions;

import dev.velix.imperat.CommandDispatcher;
import dev.velix.imperat.CommandSource;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.UsageParameter;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.resolvers.PermissionResolver;
import dev.velix.imperat.resolvers.SuggestionResolver;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

@ApiStatus.Internal
final class AutoCompleterImpl<C> implements AutoCompleter<C> {

	private final Command<C> command;

	AutoCompleterImpl(Command<C> command) {
		this.command = command;
	}

	/**
	 * @return The auto-completion command
	 */
	@Override
	public Command<C> getCommand() {
		return command;
	}

	private static @NotNull CompletionArg getLastArg(String[] args) {
		if (args.length == 0) return new CompletionArg(null, -1);
		int index = args.length - 1;
		String result = args[args.length - 1];
		if (result.isEmpty() || result.equals(" "))
			result = null;

		return new CompletionArg(result, index);
	}

	/**
	 * Autocompletes an argument from the whole position of the
	 * argument-raw input
	 *
	 * @param dispatcher the command dispatcher
	 * @param sender     the sender writing the command
	 * @param args       the args for raw input
	 * @return the auto-completed results
	 */
	@Override
	public List<String> autoComplete(CommandDispatcher<C> dispatcher,
	                                 CommandSource<C> sender, String[] args) {
		CompletionArg argToComplete = getLastArg(args);
		return autoCompleteArgument(dispatcher, sender, argToComplete, args);
	}


	/**
	 * Autocompletes an argument from the whole position of the
	 * argument-raw input
	 *
	 * @param dispatcher the command dispatcher
	 * @param sender     the sender of the auto-completion
	 * @param currentArg the arg being completed
	 * @param args       the args for raw input
	 * @return the auto-completed results
	 */
	@Override
	public List<String> autoCompleteArgument(CommandDispatcher<C> dispatcher,
	                                         CommandSource<C> sender,
	                                         CompletionArg currentArg,
	                                         String[] args) {

		AutoCompleteList results = new AutoCompleteList();

		final PermissionResolver<C> permResolver = dispatcher.getPermissionResolver();
		if (!command.isIgnoringACPerms() &&
				  !permResolver.hasPermission(sender, command.getPermission())) {
			return Collections.emptyList();
		}

		ArgumentQueue queue = ArgumentQueue.parse(args);
		List<CommandUsage<C>> closestUsages = getClosestUsages(args);
		int index = currentArg.index();
		if (index == -1)
			index = 0;

		for (CommandUsage<C> usage : closestUsages) {
			if (index < 0 || index >= usage.getMaxLength()) continue;
			UsageParameter parameter = usage.getParameters().get(index);
			if (parameter.isCommand()) {
				if (!command.isIgnoringACPerms() &&
						  !permResolver.hasPermission(sender, parameter.asCommand().getPermission()))
					continue;

				results.add(parameter.getName());
			} else {
				SuggestionResolver<C, ?> resolver = dispatcher.getSuggestionResolver(parameter);
				if (resolver != null) {
					results.addAll(resolver.autoComplete(command, sender.getOrigin(),
							  queue, parameter, currentArg));
				}
			}
		}

		return results.getResults();
	}


	private List<CommandUsage<C>> getClosestUsages(String[] args) {

		return command.lookup()
				  .findUsages((usage) -> {
					  if (args.length >= usage.getMaxLength()) {
						  for (int i = 0; i < usage.getMaxLength(); i++) {
							  UsageParameter parameter = usage.getParameters().get(i);
							  if (!parameter.isCommand()) continue;

							  if (i >= args.length) return false;
							  String corresponding = args[i];
							  if (!parameter.asCommand().hasName(corresponding))
								  return false;
						  }
					  }
					  return usage.getMaxLength() >= args.length;
				  });
	}


}
