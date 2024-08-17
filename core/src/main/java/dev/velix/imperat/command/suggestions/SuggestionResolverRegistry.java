package dev.velix.imperat.command.suggestions;

import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.util.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public final class SuggestionResolverRegistry<C> extends Registry<Class<?>, SuggestionResolver<C, ?>> {
	
	private final Map<String, SuggestionResolver<C, ?>> resolversPerArg;
	
	public SuggestionResolverRegistry() {
		super();
		resolversPerArg = new HashMap<>();
	}
	
	public <T> void registerResolver(SuggestionResolver<C, T> suggestionResolver) {
		setData(suggestionResolver.getType(), suggestionResolver);
	}
	
	public <T> void registerArgumentResolver(String argName,
	                                         SuggestionResolver<C, T> suggestionResolver) {
		resolversPerArg.put(argName, suggestionResolver);
	}
	
	public @Nullable <T> SuggestionResolver<C, ?> getResolver(Class<T> clazz) {
		return getData(clazz).orElse(null);
	}
	
	@SuppressWarnings("unchecked")
	public @Nullable <T> SuggestionResolver<C, T> getArgumentResolver(String argName) {
		return (SuggestionResolver<C, T>) resolversPerArg.get(argName);
	}
	
}
