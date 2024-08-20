package dev.velix.imperat.context.internal;

import dev.velix.imperat.resolvers.ContextResolver;
import dev.velix.imperat.util.Registry;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@ApiStatus.AvailableSince("1.0.0")
public final class ContextResolverRegistry<C> extends Registry<Class<?>, ContextResolver<C, ?>> {

    private ContextResolverFactory<C> factory;

    private ContextResolverRegistry() {
        super();
        factory = (parameter ->
                getResolver(parameter.getType()));
    }

    public static <C> ContextResolverRegistry<C> createDefault() {
        return new ContextResolverRegistry<>();
    }

    public <T> void registerResolver(Class<T> clazz, ContextResolver<C, T> resolver) {
        setData(clazz, resolver);
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable ContextResolver<C, T> getResolver(Class<T> type) {
        return (ContextResolver<C, T>) getData(type).orElse(null);
    }


}
