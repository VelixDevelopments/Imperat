package dev.velix.imperat.context;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.internal.sur.Cursor;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.SourceAnswerException;
import dev.velix.imperat.resolvers.ValueResolver;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class EnumValueResolver<S extends Source, E extends Enum<E>> implements ValueResolver<S, E> {
    
    private final Class<E> enumType;
    
    public EnumValueResolver(Class<E> enumType) {
        this.enumType = enumType;
    }
    
    @Override
    public E resolve(S source,
                     Context<S> context,
                     String raw,
                     Cursor cursor,
                     CommandParameter parameter
    ) throws ImperatException {
        try {
            return Enum.valueOf(enumType, raw.toUpperCase());
        } catch (EnumConstantNotPresentException ex) {
            throw new SourceAnswerException("Invalid " + enumType.getSimpleName() + " '" + raw + "'");
        }
    }
}
