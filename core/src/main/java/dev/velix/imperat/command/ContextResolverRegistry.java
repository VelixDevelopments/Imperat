package dev.velix.imperat.command;

import dev.velix.imperat.ImperatConfig;
import dev.velix.imperat.annotations.base.element.ParameterElement;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.help.CommandHelp;
import dev.velix.imperat.resolvers.ContextResolver;
import dev.velix.imperat.util.Registry;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.*;

import java.lang.reflect.Type;
import java.util.Optional;

@ApiStatus.AvailableSince("1.0.0")
public final class ContextResolverRegistry<S extends Source> extends Registry<Type, ContextResolver<S, ?>> {

    private final Registry<Type, ContextResolverFactory<S, ?>> factories = new Registry<>();

    private ContextResolverRegistry(final ImperatConfig<S> config) {
        super();
        this.registerResolver(TypeWrap.of(CommandHelp.class).getType(), (ctx, param) -> new CommandHelp(config, ctx));
    }

    public static <S extends Source> ContextResolverRegistry<S> createDefault(final ImperatConfig<S> imperat) {
        return new ContextResolverRegistry<>(imperat);
    }

    public <T> void registerResolver(Type type, ContextResolver<S, T> resolver) {
        setData(type, resolver);
    }

    public <T> void registerFactory(Type type, ContextResolverFactory<S, T> factory) {
        factories.setData(type, factory);
    }

    public Optional<ContextResolverFactory<S, ?>> getFactoryFor(Type type) {
        return factories.getData(type);
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable ContextResolver<S, T> getContextResolver(Type type, @Nullable ParameterElement element) {
        //we search for factories mainly
        ContextResolverFactory<S, T> factory = (ContextResolverFactory<S, T>) getFactoryFor(type).orElse(null);
        if (factory == null) {
            return factories.getData(type)
                .map((defaultFactory) -> ((ContextResolverFactory<S, T>) defaultFactory).create(type, element))
                .orElse((ContextResolver<S, T>) getData(type).orElse(null));
        }
        return factory.create(type, element);
    }

    public <T> @Nullable ContextResolver<S, T> getResolverWithoutParameterElement(Type type) {
        return getContextResolver(type, null);
    }

}
