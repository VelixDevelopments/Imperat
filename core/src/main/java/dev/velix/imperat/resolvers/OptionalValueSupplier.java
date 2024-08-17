package dev.velix.imperat.resolvers;

import dev.velix.imperat.command.parameters.UsageParameter;
import dev.velix.imperat.context.Context;

public interface OptionalValueSupplier<C, T> {
	
	@SuppressWarnings("unchecked")
	static <C, T> OptionalValueSupplier<C, T> of(T def) {
		return new OptionalValueSupplier<>() {
			@Override
			public Class<T> getValueType() {
				return (Class<T>) def.getClass();
			}
			
			@Override
			public T supply(Context<C> context) {
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
	 * usage parameters {@link UsageParameter}
	 *
	 * @param context the context
	 * @return the resolved default value
	 */
	T supply(Context<C> context);
	
}
