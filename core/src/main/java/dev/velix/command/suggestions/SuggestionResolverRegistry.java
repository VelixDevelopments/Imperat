package dev.velix.command.suggestions;

import dev.velix.command.parameters.CommandParameter;
import dev.velix.context.Source;
import dev.velix.context.SuggestionContext;
import dev.velix.resolvers.SuggestionResolver;
import dev.velix.util.Registry;
import dev.velix.util.TypeUtility;
import dev.velix.util.TypeWrap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;

@ApiStatus.Internal
public final class SuggestionResolverRegistry<S extends Source> extends Registry<Type, SuggestionResolver<S, ?>> {
    
    private final Map<String, SuggestionResolver<S, ?>> resolversPerName;
    
    private final EnumSuggestionResolver enumSuggestionResolver = new EnumSuggestionResolver();
    
    private SuggestionResolverRegistry() {
        super();
        resolversPerName = new HashMap<>();
    }
    
    public static <S extends Source> SuggestionResolverRegistry<S> createDefault() {
        return new SuggestionResolverRegistry<>();
    }
    
    @SuppressWarnings({"unchecked"})
    public <T> void registerResolver(SuggestionResolver<S, T> suggestionResolver) {
        Type resolverType = suggestionResolver.getType().getType();
        if (TypeUtility.areRelatedTypes(resolverType, Enum.class)) {
            //we don't register enum related resolvers
            enumSuggestionResolver.registerEnumResolver((Class<? extends Enum<?>>) resolverType);
            return;
        }
        setData(resolverType, suggestionResolver);
    }
    
    public <T> void registerNamedResolver(String name,
                                          SuggestionResolver<S, T> suggestionResolver) {
        resolversPerName.put(name, suggestionResolver);
    }
    
    public @Nullable SuggestionResolver<S, ?> getResolver(Type type) {
        if (TypeUtility.areRelatedTypes(type, Enum.class)) {
            return enumSuggestionResolver;
        }
        return getData(type).orElseGet(() -> {
            for (var resolverByType : this.getAll()) {
                if (resolverByType.getType().isSupertypeOf(type)) {
                    return resolverByType;
                }
            }
            return null;
        });
    }
    
    @SuppressWarnings("unchecked")
    public @Nullable <T> SuggestionResolver<S, T> getResolverByName(String name) {
        return (SuggestionResolver<S, T>) resolversPerName.get(name);
    }
    
    @SuppressWarnings({"rawtypes"})
    final class EnumSuggestionResolver implements SuggestionResolver<S, Enum> {
        private final Map<Type, List<String>> PRE_LOADED_ENUMS = new HashMap<>();
        
        public void registerEnumResolver(Class<? extends Enum<?>> raw) {
            PRE_LOADED_ENUMS.computeIfAbsent(raw,
                    (v) -> Arrays.stream(raw.getEnumConstants()).map(Enum::name).toList());
        }
        
        private Optional<List<String>> getResults(Type type) {
            return Optional.ofNullable(PRE_LOADED_ENUMS.get(type));
        }
        
        @Override
        public TypeWrap<Enum> getType() {
            return TypeWrap.of(Enum.class);
        }
        
        @Override
        public List<String> autoComplete(SuggestionContext<S> context, CommandParameter parameterToComplete) {
            Type type = this.getType().getType();
            return getResults(type)
                    .orElse(Collections.emptyList());
        }
    }
    
}
