package dev.velix.imperat.supplier;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.util.Preconditions;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public interface OptionalValueSupplier<T> {

    @SuppressWarnings("unchecked")
    static <T> OptionalValueSupplier<T> of(@NotNull T def) {
        Preconditions.notNull(def, "default cannot be null, use `OptionalValueSupplier#empty` instead");
        return new OptionalValueSupplier<>() {
            @Override
            public TypeWrap<T> getValueType() {
                return (TypeWrap<T>) TypeWrap.of(def.getClass());
            }

            @Override
            public <S extends Source> T supply(S source) {
                return def;
            }
        };
    }
    
    static @NotNull <T> OptionalValueSupplier<T> empty(TypeWrap<T> type) {
        return new OptionalValueSupplier<>() {
            @Override
            public TypeWrap<T> getValueType() {
                return type;
            }
            
            @Override
            public <S extends Source> @Nullable T supply(S source) {
                return null;
            }
        };
    }
    
    
    default Type reflectionType() {
        return getValueType().getType();
    }
    
    /**
     * @return The type of value to supply
     */
    TypeWrap<T> getValueType();

    /**
     * Supplies a default-value for optional
     * usage parameters {@link CommandParameter}
     *
     * @param source the context
     * @return the resolved default value
     */
    @Nullable <S extends Source> T supply(S source);

}
