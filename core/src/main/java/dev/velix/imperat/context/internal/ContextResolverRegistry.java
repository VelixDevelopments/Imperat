package dev.velix.imperat.context.internal;

import dev.velix.imperat.context.Source;
import dev.velix.imperat.resolvers.ContextResolver;
import dev.velix.imperat.util.Registry;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@Getter
@Setter
@ApiStatus.AvailableSince("1.0.0")
public final class ContextResolverRegistry<S extends Source> extends Registry<Type, ContextResolver<S, ?>> {
    
    private ContextResolverFactory<S> factory;
    
    private ContextResolverRegistry() {
        super();
        factory = (parameter ->
        {
            assert parameter != null;
            return getResolver(parameter.getType());
        });
    }
    
    public static <S extends Source> ContextResolverRegistry<S> createDefault() {
        return new ContextResolverRegistry<>();
    }
    
    public <T> void registerResolver(Type type, ContextResolver<S, T> resolver) {
        setData(type, resolver);
    }
    
    @SuppressWarnings("unchecked")
    public <T> @Nullable ContextResolver<S, T> getResolver(Type type) {
        return (ContextResolver<S, T>) getData(type).orElse(null);
    }
    
    
}
