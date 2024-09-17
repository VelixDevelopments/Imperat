package dev.velix.context;

import dev.velix.command.parameters.CommandParameter;
import dev.velix.context.internal.sur.Cursor;
import dev.velix.exception.ImperatException;
import dev.velix.exception.SourceException;
import dev.velix.resolvers.ValueResolver;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class EnumValueResolver<S extends Source, E extends Enum<E>> implements ValueResolver<S, E> {
    
    private final Class<E> enumType;
    
    public EnumValueResolver(Class<E> enumType) {
        this.enumType = enumType;
    }
    
    @Override
    public E resolve(
            ExecutionContext<S> context,
            CommandParameter parameter,
            Cursor cursor,
            String raw
    ) throws ImperatException {
        try {
            return Enum.valueOf(enumType, raw.toUpperCase());
        } catch (EnumConstantNotPresentException ex) {
            throw new SourceException("Invalid " + enumType.getSimpleName() + " '" + raw + "'");
        }
    }
}
