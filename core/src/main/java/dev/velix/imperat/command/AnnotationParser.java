package dev.velix.imperat.command;

import dev.velix.imperat.CommandDispatcher;
import dev.velix.imperat.annotations.AnnotationReader;
import dev.velix.imperat.annotations.CommandAnnotationRegistry;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
final class AnnotationParser<C> {

	private final CommandDispatcher<C> dispatcher;
	private final CommandAnnotationRegistry annotationRegistry;

	AnnotationParser(CommandDispatcher<C> dispatcher) {
		this.dispatcher = dispatcher;
		this.annotationRegistry = new CommandAnnotationRegistry();
	}

	@SuppressWarnings("unchecked")
	<T> void parseCommandClass(T instance) {
		Class<T> instanceClazz = (Class<T>) instance.getClass();
		AnnotationReader reader = AnnotationReader.read(annotationRegistry, instanceClazz);

		//TODO design structure for parsing !!
		/*CommandLoader<C> commandLoader = new CommandLoader<>(instanceClazz);
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
		}*/
		//dispatcher.registerCommand(command);
	}

	public CommandAnnotationRegistry getRegistry() {
		return annotationRegistry;
	}
}
