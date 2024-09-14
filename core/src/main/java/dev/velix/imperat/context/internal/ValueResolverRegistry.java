package dev.velix.imperat.context.internal;

import dev.velix.imperat.context.EnumValueResolver;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.exception.SourceException;
import dev.velix.imperat.resolvers.ValueResolver;
import dev.velix.imperat.util.Registry;
import dev.velix.imperat.util.TypeUtility;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

@ApiStatus.Internal
public final class ValueResolverRegistry<S extends Source> extends Registry<Type, ValueResolver<S, ?>> {
    
    
    private ValueResolverRegistry() {
        super();
        registerResolver(String.class, ((context, parameter, cursor, raw) -> raw));
        registerResolver(Integer.class, (context, parameter, cursor, raw) -> {
            if (TypeUtility.isInteger(raw)) {
                return Integer.parseInt(raw);
            } else {
                throw exception(raw, Integer.class);
            }
        });
        registerResolver(Long.class, (context, parameter, cursor, raw) -> {
            if (TypeUtility.isLong(raw)) {
                return Long.parseLong(raw);
            } else {
                throw exception(raw, Long.class);
            }
        });
        registerResolver(Boolean.class, (context, parameter, cursor, raw) -> {
            if (TypeUtility.isBoolean(raw)) {
                return Boolean.valueOf(raw);
            } else {
                throw exception(raw, Boolean.class);
            }
        });
        registerResolver(Double.class, (context, parameter, cursor, raw) -> {
            if (TypeUtility.isDouble(raw)) {
                return Double.parseDouble(raw);
            } else {
                throw exception(raw, Double.class);
            }
        });
        registerEnumResolver(TimeUnit.class);
    }
    
    public static <S extends Source> ValueResolverRegistry<S> createDefault() {
        return new ValueResolverRegistry<>();
    }
    
    private SourceException exception(String raw,
                                      Class<?> clazzRequired) {
        return new SourceException(
                "Error while parsing argument '%s', It's not a valid %s", raw, clazzRequired.getSimpleName()
        );
    }
    
    public <T> void registerResolver(Type type, ValueResolver<S, T> resolver) {
        setData(type, resolver);
    }
    
    public <E extends Enum<E>> void registerEnumResolver(Class<E> enumClass) {
        registerResolver(enumClass, new EnumValueResolver<>(enumClass));
    }
    
    @SuppressWarnings("unchecked")
    public <T> ValueResolver<S, T> getResolver(Type type) {
        return (ValueResolver<S, T>) getData(TypeUtility.primitiveToBoxed(type)).orElseGet(() -> {
            for (var registeredType : getKeys()) {
                if (TypeUtility.areRelatedTypes(type, registeredType)) {
                    return getData(registeredType).orElse(null);
                }
            }
            return null;
        });
    }
    
}
