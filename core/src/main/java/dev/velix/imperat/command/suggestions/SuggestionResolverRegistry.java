package dev.velix.imperat.command.suggestions;

import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.util.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public final class SuggestionResolverRegistry<C> extends Registry<Type, SuggestionResolver<C, ?>> {

    private final Map<String, SuggestionResolver<C, ?>> resolversPerName;

    private SuggestionResolverRegistry() {
        super();
        resolversPerName = new HashMap<>();
    }

    public static <C> SuggestionResolverRegistry<C> createDefault() {
        return new SuggestionResolverRegistry<>();
    }

    public <T> void registerResolver(SuggestionResolver<C, T> suggestionResolver) {
        setData(suggestionResolver.getType().getType(), suggestionResolver);
    }

    public <T> void registerNamedResolver(String name,
                                          SuggestionResolver<C, T> suggestionResolver) {
        resolversPerName.put(name, suggestionResolver);
    }

    public @Nullable <T> SuggestionResolver<C, ?> getResolver(Class<T> clazz) {
        return getData(clazz).orElse(null);
    }

    @SuppressWarnings("unchecked")
    public @Nullable <T> SuggestionResolver<C, T> getResolverByName(String name) {
        return (SuggestionResolver<C, T>) resolversPerName.get(name);
    }

}
