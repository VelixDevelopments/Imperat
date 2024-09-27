package dev.velix.imperat.supplier.defaults;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.supplier.OptionalValueSupplier;

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
