package dev.velix.imperat.supplier.defaults;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.supplier.OptionalValueSupplier;
import dev.velix.imperat.util.TypeWrap;

public final class BooleanValueSupplier implements OptionalValueSupplier<Boolean> {

    /**
     * @return The valueType of value to supply
     */
    @Override
    public TypeWrap<Boolean> getValueType() {
        return TypeWrap.of(Boolean.class);
    }

    /**
     * Supplies a default-value for optional
     * usage parameters {@link CommandParameter}
     *
     * @param source the context
     * @return the resolved default value
     */
    @Override
    public <S extends Source> Boolean supply(S source) {
        return false;
    }

}
