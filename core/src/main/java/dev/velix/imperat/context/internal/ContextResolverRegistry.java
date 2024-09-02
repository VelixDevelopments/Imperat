package dev.velix.imperat.context.internal;

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
public final class ContextResolverRegistry<C> extends Registry<Type, ContextResolver<C, ?>> {

    private ContextResolverFactory<C> factory;

    private ContextResolverRegistry() {
        super();
        factory = (parameter ->
        {
	        assert parameter != null;
	        return getResolver(parameter.getType());
        });
    }

    public static <C> ContextResolverRegistry<C> createDefault() {
        return new ContextResolverRegistry<>();
    }

    public <T> void registerResolver(Type type, ContextResolver<C, T> resolver) {
        setData(type, resolver);
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable ContextResolver<C, T> getResolver(Type type) {
        return (ContextResolver<C, T>) getData(type).orElse(null);
    }


}
