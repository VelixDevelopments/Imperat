package dev.velix.imperat.command.parameters;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.parameters.type.BaseParameterType;
import dev.velix.imperat.command.parameters.type.ParameterType;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.SourceException;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.*;
import java.lang.reflect.Type;
import java.util.Set;

public final class ConstrainedParameterTypeDecorator<S extends Source, T> extends BaseParameterType<S,  T> {

    private final ParameterType<S, T> original;

    private final boolean caseSensitive;
    private final Set<String> allowedValues;

    private ConstrainedParameterTypeDecorator(ParameterType<S, T> original, Set<String> allowedValues, boolean caseSensitive) {
        super(original.wrappedType());
        this.original = original;
        this.allowedValues = allowedValues;
        this.caseSensitive = caseSensitive;
    }

    public static <S extends Source, T> ConstrainedParameterTypeDecorator<S, T> of(ParameterType<S, T> original, Set<String> allowedValues, boolean caseSensitive) {
        return new ConstrainedParameterTypeDecorator<>(original, allowedValues, caseSensitive);
    }

    public static <S extends Source, T> ConstrainedParameterTypeDecorator<S, T> of(ParameterType<S, T> original, Set<String> allowedValues) {
        return new ConstrainedParameterTypeDecorator<>(original, allowedValues, true);
    }

    @Override
    public @Nullable T resolve(ExecutionContext<S> context, @NotNull CommandInputStream<S> commandInputStream) throws ImperatException {
        String input = commandInputStream.currentRaw().orElse(null);
        if(input == null)
            return original.resolve(context, commandInputStream);

        if(ConstrainedParameterTypeDecorator.contains(input, allowedValues, caseSensitive)) {
            return original.resolve(context, commandInputStream);
        }else {
            throw new SourceException("Input '%s' is not one of: [" + String.join(",",  allowedValues) + "]", input);
        }
    }

    private static boolean contains(String input, Set<String> allowedValues, boolean caseSensitive) {
        if(caseSensitive)
            return allowedValues.contains(input);

        for(String value : allowedValues) {
            if(input.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean isRelatedToType(Type type) {
        return original.isRelatedToType(type);
    }

    @Override
    public boolean equalsExactly(Type type) {
        return original.equalsExactly(type);
    }

    @Override
    public TypeWrap<T> wrappedType() {
        return original.wrappedType();
    }

    @Override
    public @NotNull T fromString(Imperat<S> imperat, String input) {
        return original.fromString(imperat, input);
    }

}
