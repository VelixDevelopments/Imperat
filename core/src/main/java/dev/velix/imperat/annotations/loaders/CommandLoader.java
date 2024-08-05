package dev.velix.imperat.annotations.loaders;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.annotations.AnnotationLoader;
import dev.velix.imperat.annotations.types.Description;
import dev.velix.imperat.annotations.types.Permission;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class CommandLoader<C> implements AnnotationLoader<C, Command<C>> {

	private final Class<?> aClass;
	public CommandLoader(Class<?> aClass) {
		this.aClass = aClass;
	}



	@Override
	public Command<C> load(@Nullable Command<C> existingCommand) {

		assert existingCommand == null;

		dev.velix.imperat.annotations.types.Command annotation =
				  aClass.getAnnotation(dev.velix.imperat.annotations.types.Command.class);
		if(annotation == null) {
			throw new IllegalStateException("Cannot find @Command for the class '" + aClass.getName() + "'");
		}

		final String[] values = annotation.value();
		System.out.println("VALUES SIZE= " + values.length);
		List<String> aliases = new ArrayList<>();

		for (int i = 1; i < values.length; i++) {
			aliases.add(values[i]);
		}
		Command<C> command =  Command.createCommand(values[0]);
		command.addAliases(aliases);

		Permission perm = aClass.getAnnotation(Permission.class);
		Description description = aClass.getAnnotation(Description.class);

		if(perm != null) {
			command.setPermission(perm.value());
		}
		if(description != null) {
			command.setDescription(description.value());
		}

		return command;
	}


}
