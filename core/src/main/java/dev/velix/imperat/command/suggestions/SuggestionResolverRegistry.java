package dev.velix.imperat.command.suggestions;

import dev.velix.imperat.context.Source;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.util.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public final class SuggestionResolverRegistry<S extends Source> extends Registry<Type, SuggestionResolver<S, ?>> {
    
    private final Map<String, SuggestionResolver<S, ?>> resolversPerName;
    
    private SuggestionResolverRegistry() {
        super();
        resolversPerName = new HashMap<>();
    }
    
    public static <S extends Source> SuggestionResolverRegistry<S> createDefault() {
        return new SuggestionResolverRegistry<>();
    }
    
    public <T> void registerResolver(SuggestionResolver<S, T> suggestionResolver) {
        setData(suggestionResolver.getType().getType(), suggestionResolver);
    }
    
    public <T> void registerNamedResolver(String name,
                                          SuggestionResolver<S, T> suggestionResolver) {
        resolversPerName.put(name, suggestionResolver);
    }
    
    public @Nullable <T> SuggestionResolver<S, ?> getResolver(Class<T> clazz) {
        return getData(clazz).orElse(null);
    }
    
    @SuppressWarnings("unchecked")
    public @Nullable <T> SuggestionResolver<S, T> getResolverByName(String name) {
        return (SuggestionResolver<S, T>) resolversPerName.get(name);
    }
    
}
