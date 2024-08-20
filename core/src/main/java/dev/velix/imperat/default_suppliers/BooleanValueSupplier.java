package dev.velix.imperat.default_suppliers;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.resolvers.OptionalValueSupplier;

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
	public <C> Boolean supply(Context<C> context) {
		return false;
	}
}
