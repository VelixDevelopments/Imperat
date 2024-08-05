package dev.velix.imperat;

import dev.velix.imperat.annotations.loaders.CommandUsageLoader;
import dev.velix.imperat.annotations.types.methods.SubCommand;
import dev.velix.imperat.annotations.loaders.CommandLoader;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ApiStatus.Internal
public final class AnnotationParser<C> {

	private final CommandDispatcher<C> dispatcher;

	AnnotationParser(CommandDispatcher<C> dispatcher) {
		this.dispatcher = dispatcher;
	}

	@SuppressWarnings("unchecked")
	<T> void parseCommandClass(T instance) {
		Class<T> instanceClazz = (Class<T>) instance.getClass();

		CommandLoader<C> commandLoader = new CommandLoader<>(instanceClazz);
		Command<C> command = commandLoader.load(null);

		for (Method method : instanceClazz.getDeclaredMethods()) {

			CommandUsageLoader<C> commandUsageLoader = new CommandUsageLoader<>(
					  dispatcher,
					  instance, instanceClazz, method
			);
			CommandUsage<C> usage = commandUsageLoader.load(command);
			if (usage == null) continue;

			SubCommand subCommand = method.getAnnotation(SubCommand.class);

			if (subCommand != null) {
				final String[] subNames = subCommand.value();
				String originalName = subNames[0];
				List<String> aliases = new ArrayList<>(Arrays.asList(subNames).subList(1, subNames.length));
				command.addSubCommandUsage(originalName, aliases, usage, subCommand.attachDirectly());
				continue;
			}

			command.addUsage(usage);
		}

		dispatcher.registerCommand(command);
	}

}
