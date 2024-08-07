package dev.velix.imperat.annotations.types;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Command {

	/**
	 * @return The names of this command
	 * first element is the unique name of the command
	 * others are going to be considered the aliases
	 */
	@NotNull String[] value();

	/**
	 * @return Whether to ignore the permission checks
	 * while auto-completing or not
	 * @see dev.velix.imperat.command.Command#ignoreACPermissions(boolean)
	 */
	boolean ignoreAutoCompletionPermission() default false;
}