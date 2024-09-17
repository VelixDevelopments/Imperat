package dev.velix.supplier;

import dev.velix.command.parameters.CommandParameter;
import dev.velix.context.Context;
import dev.velix.context.Source;

public interface OptionalValueSupplier<T> {
    
    @SuppressWarnings("unchecked")
    static <T> OptionalValueSupplier<T> of(T def) {
        if (def == null) return null;
        return new OptionalValueSupplier<>() {
            @Override
            public Class<T> getValueType() {
                return (Class<T>) def.getClass();
            }
            
            @Override
            public <S extends Source> T supply(Context<S> context) {
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
     * @param context the context
     * @return the resolved default value
     */
    <S extends Source> T supply(Context<S> context);
    
}
