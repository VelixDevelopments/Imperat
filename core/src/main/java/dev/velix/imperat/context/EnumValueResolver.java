package dev.velix.imperat.context;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.internal.sur.Cursor;
import dev.velix.imperat.exceptions.CommandException;
import dev.velix.imperat.exceptions.context.ContextResolveException;
import dev.velix.imperat.resolvers.ValueResolver;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class EnumValueResolver<C, E extends Enum<E>> implements ValueResolver<C, E> {

    private final Class<E> enumType;

    public EnumValueResolver(Class<E> enumType) {
        this.enumType = enumType;
    }

    @Override
    public E resolve(Source<C> source,
                     Context<C> context,
                     String raw,
                     Cursor cursor,
                     CommandParameter parameter
    ) throws CommandException {
        try {
            return Enum.valueOf(enumType, raw.toUpperCase());
        } catch (EnumConstantNotPresentException ex) {
            throw new ContextResolveException("Invalid " + enumType.getSimpleName() + " '" + raw + "'");
        }
    }
}
