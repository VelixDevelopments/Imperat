package dev.velix.imperat.annotations.loaders;

import dev.velix.imperat.annotations.types.Description;
import dev.velix.imperat.annotations.types.Permission;
import dev.velix.imperat.command.Command;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Deprecated
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
		if (annotation == null) {
			throw new IllegalStateException("Cannot find @Command for the class '" + aClass.getName() + "'");
		}

		final String[] values = annotation.value();

		List<String> aliases = new ArrayList<>(Arrays.asList(values).subList(1, values.length));
		Command<C> command = Command.createCommand(values[0]);
		command.ignoreACPermissions(annotation.ignoreAutoCompletionPermission());
		command.addAliases(aliases);

		Permission perm = aClass.getAnnotation(Permission.class);
		Description description = aClass.getAnnotation(Description.class);


		if (perm != null) {
			command.setPermission(perm.value());
		}
		if (description != null) {
			command.setDescription(description.value());
		}

		return command;
	}


}
