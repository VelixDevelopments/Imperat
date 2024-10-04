package dev.velix.imperat.command;

import dev.velix.imperat.context.Source;
import dev.velix.imperat.resolvers.SourceResolver;
import dev.velix.imperat.util.Registry;

import java.lang.reflect.Type;

public final class SourceResolverRegistry<S extends Source> extends Registry<Type, SourceResolver<S, ?>> {
	
	SourceResolverRegistry() {
	
	}
	
	public static <S extends Source> SourceResolverRegistry<S> createDefault() {
		return new SourceResolverRegistry<>();
	}
	
}
