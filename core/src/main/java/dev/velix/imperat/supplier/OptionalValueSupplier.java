package dev.velix.imperat.supplier;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.Nullable;

public interface OptionalValueSupplier<T> {

    @SuppressWarnings("unchecked")
    static <T> OptionalValueSupplier<T> of(T def) {
        return new OptionalValueSupplier<>() {
            @Override
            public Class<T> getValueType() {
                return (Class<T>) def.getClass();
            }

            @Override
            public <S extends Source> T supply(S source) {
                return def;
            }
        };
    }

    /**
     * @return The type of value to supply
     */
    Class<T> getValueType();

    /**
     * Supplies a default-value for optional
     * usage parameters {@link CommandParameter}
     *
     * @param source the context
     * @return the resolved default value
     */
    @Nullable <S extends Source> T supply(S source);

}
