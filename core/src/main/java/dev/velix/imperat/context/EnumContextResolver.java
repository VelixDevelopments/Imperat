package dev.velix.imperat.context;

import dev.velix.imperat.CommandSource;
import dev.velix.imperat.exceptions.CommandException;
import dev.velix.imperat.exceptions.context.ContextResolveException;
import dev.velix.imperat.resolvers.ContextResolver;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class EnumContextResolver<C, E extends Enum<E>> implements ContextResolver<C, E> {

	private final Class<E> enumType;

	public EnumContextResolver(Class<E> enumType) {
		this.enumType = enumType;
	}

	/**
	 * @param source  the source of the command
	 * @param context the context for the command
	 * @param raw     the required parameter of the command.
	 * @return the resolved output from the input object
	 */
	@Override
	public E resolve(CommandSource<C> source, Context<C> context, String raw) throws CommandException {
		try {
			return Enum.valueOf(enumType, raw.toUpperCase());
		} catch (EnumConstantNotPresentException ex) {
			throw new ContextResolveException("Invalid " + enumType.getSimpleName() + " '" + raw + "'");
		}
	}
}
