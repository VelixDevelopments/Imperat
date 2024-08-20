package dev.velix.imperat.resolvers;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Context;

public interface OptionalValueSupplier<T> {
	
	@SuppressWarnings("unchecked")
	static <T> OptionalValueSupplier<T> of(T def) {
		return new OptionalValueSupplier<>() {
			@Override
			public Class<T> getValueType() {
				return (Class<T>) def.getClass();
			}
			
			@Override
			public <C> T supply(Context<C> context) {
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
	<C> T supply(Context<C> context);
	
}
