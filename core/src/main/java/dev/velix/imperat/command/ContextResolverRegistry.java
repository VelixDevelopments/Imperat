package dev.velix.imperat.command;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.help.CommandHelp;
import dev.velix.imperat.resolvers.ContextResolver;
import dev.velix.imperat.util.Registry;
import dev.velix.imperat.util.TypeUtility;
import dev.velix.imperat.util.TypeWrap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@Getter
@Setter
@ApiStatus.AvailableSince("1.0.0")
final class ContextResolverRegistry<S extends Source> extends Registry<Type, ContextResolver<S, ?>> {
    
    private ContextResolverFactory<S> factory;
    
    private ContextResolverRegistry(Imperat<S> imperat) {
        super();
        factory = (parameter -> {
            assert parameter != null;
            return getResolver(parameter.getType());
        });
        this.registerResolver(TypeWrap.of(CommandHelp.class).getType(), (ctx, param) -> new CommandHelp(imperat, ctx));
    }
    
    public static <S extends Source> ContextResolverRegistry<S> createDefault(Imperat<S> imperat) {
        return new ContextResolverRegistry<>(imperat);
    }
    
    public <T> void registerResolver(Type type, ContextResolver<S, T> resolver) {
        setData(type, resolver);
    }
    
    
    @SuppressWarnings("unchecked")
    public <T> @Nullable ContextResolver<S, T> getResolver(Type type) {
        return (ContextResolver<S, T>) getData(type).orElseGet(() -> {
            for (var registeredType : getKeys()) {
                if (TypeUtility.areRelatedTypes(type, registeredType)) {
                    return getData(registeredType).orElse(null);
                }
            }
            return null;
        });
    }
    
    
}
