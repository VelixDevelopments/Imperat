package dev.velix.imperat.command;

import dev.velix.imperat.resolvers.OptionalValueSupplier;
import dev.velix.imperat.util.Registry;

final class OptionalDefaultValueRegistry<C> extends Registry<OptionalDefaultValueRegistry.ParamId, OptionalValueSupplier<C, ?>> {


	record ParamId(String name, Class<?> type) {

	}
}
