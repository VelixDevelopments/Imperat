package dev.zafrias.imperat.resolvers;

import dev.zafrias.imperat.CommandSource;
import dev.zafrias.imperat.Resolver;
import dev.zafrias.imperat.Result;

/**
 * Represents a functional way of checking for the permissions
 * of the command source/sender.
 */
public interface PermissionResolver<C> extends Resolver<C, String, Boolean> {

	/**
	 * @param source     the source of the command (console or other)
	 * @param permission the permission
	 * @return whether this command source/sender has a specific permission
	 */
	boolean hasPermission(CommandSource<C> source, String permission);

	@Override
	default Result<Boolean> resolve(CommandSource<C> source, String input) {
		return Result.success(source.isConsole()
				  || hasPermission(source, input));
	}

}
