package dev.velix.imperat.resolvers;

import dev.velix.imperat.context.Source;
import dev.velix.imperat.exception.ImperatException;
import org.jetbrains.annotations.NotNull;

/**
 * An interface whose single responsibility is to resolve {@link S}
 * into {@link R} to allow custom command sources
 *
 * @param <S> the default platform source
 * @param <R> the resulting source
 */
public interface SourceResolver<S extends Source, R> {
	
	/**
	 * Resolves {@link S} into {@link R}
	 *
	 * @param source the default source within the platform
	 * @return the resolved source
	 */
	@NotNull
	R resolve(S source) throws ImperatException;
	
}
