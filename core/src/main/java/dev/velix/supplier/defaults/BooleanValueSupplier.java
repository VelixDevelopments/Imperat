package dev.velix.supplier.defaults;

import dev.velix.command.parameters.CommandParameter;
import dev.velix.context.Context;
import dev.velix.context.Source;
import dev.velix.supplier.OptionalValueSupplier;

public final class BooleanValueSupplier implements OptionalValueSupplier<Boolean> {
    
    /**
     * @return The type of value to supply
     */
    @Override
    public Class<Boolean> getValueType() {
        return Boolean.class;
    }
    
    /**
     * Supplies a default-value for optional
     * usage parameters {@link CommandParameter}
     *
     * @param context the context
     * @return the resolved default value
     */
    @Override
    public <S extends Source> Boolean supply(Context<S> context) {
        return false;
    }
    
}
