package dev.velix.imperat.context.internal;

import dev.velix.imperat.context.EnumValueResolver;
import dev.velix.imperat.exceptions.context.ContextResolveException;
import dev.velix.imperat.resolvers.ValueResolver;
import dev.velix.imperat.util.Registry;
import dev.velix.imperat.util.TypeUtility;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

@ApiStatus.Internal
public final class ValueResolverRegistry<C> extends Registry<Type, ValueResolver<C, ?>> {


    private ValueResolverRegistry() {
        super();
        registerResolver(String.class, ((source, context, raw, pivot, parameter) -> raw));
        registerResolver(Integer.class, (source, context, raw, pivot, parameter) -> {
            if (TypeUtility.isInteger(raw)) {
                return Integer.parseInt(raw);
            } else {
                throw exception(raw, Integer.class);
            }
        });
        registerResolver(Long.class, (source, context, raw, pivot, parameter) -> {
            if (TypeUtility.isLong(raw)) {
                return Long.parseLong(raw);
            } else {
                throw exception(raw, Long.class);
            }
        });
        registerResolver(Boolean.class, (source, context, raw, pivot, parameter) -> {
            if (TypeUtility.isBoolean(raw)) {
                return Boolean.valueOf(raw);
            } else {
                throw exception(raw, Boolean.class);
            }
        });
        registerResolver(Double.class, (source, context, raw, pivot, parameter) -> {
            if (TypeUtility.isDouble(raw)) {
                return Double.parseDouble(raw);
            } else {
                throw exception(raw, Double.class);
            }
        });
        registerEnumResolver(TimeUnit.class);
    }

    public static <C> ValueResolverRegistry<C> createDefault() {
        return new ValueResolverRegistry<>();
    }

    private ContextResolveException exception(String raw,
                                              Class<?> clazzRequired) {
        return new ContextResolveException(
                "Error while parsing argument '%s', It's not a valid %s", raw, clazzRequired.getSimpleName()
        );
    }

    public <T> void registerResolver(Type type, ValueResolver<C, T> resolver) {
        setData(type, resolver);
    }

    public <E extends Enum<E>> void registerEnumResolver(Class<E> enumClass) {
        registerResolver(enumClass, new EnumValueResolver<>(enumClass));
    }

    @SuppressWarnings("unchecked")
    public <T> ValueResolver<C, T> getResolver(Type type) {
        return (ValueResolver<C, T>) getData(TypeUtility.primitiveToBoxed(type)).orElse(null);
    }

}
