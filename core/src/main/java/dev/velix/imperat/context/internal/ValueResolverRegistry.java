package dev.velix.imperat.context.internal;

import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.EnumValueResolver;
import dev.velix.imperat.exceptions.context.ContextResolveException;
import dev.velix.imperat.resolvers.ValueResolver;
import dev.velix.imperat.util.Registry;
import dev.velix.imperat.util.TypeUtility;
import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.TimeUnit;

@ApiStatus.Internal
public final class ValueResolverRegistry<C> extends Registry<Class<?>, ValueResolver<C, ?>> {


    private ValueResolverRegistry() {
        super();
        registerResolver(String.class, ((source, context, raw, pivot, parameter) -> raw));
        registerResolver(Integer.class, (source, context, raw, pivot, parameter) -> {
            if (TypeUtility.isInteger(raw)) {
                return Integer.parseInt(raw);
            } else {
                throw exception(context, raw, Integer.class);
            }
        });
        registerResolver(Long.class, (source, context, raw, pivot, parameter) -> {
            if (TypeUtility.isLong(raw)) {
                return Long.parseLong(raw);
            } else {
                throw exception(context, raw, Long.class);
            }
        });
        registerResolver(Boolean.class, (source, context, raw, pivot, parameter) -> {
            if (TypeUtility.isBoolean(raw)) {
                return Boolean.valueOf(raw);
            } else {
                throw exception(context, raw, Boolean.class);
            }
        });
        registerResolver(Double.class, (source, context, raw, pivot, parameter) -> {
            if (TypeUtility.isDouble(raw)) {
                return Double.parseDouble(raw);
            } else {
                throw exception(context, raw, Double.class);
            }
        });
        registerEnumResolver(TimeUnit.class);
    }

    public static <C> ValueResolverRegistry<C> createDefault() {
        return new ValueResolverRegistry<>();
    }

    private ContextResolveException exception(Context<C> context,
                                              String raw,
                                              Class<?> clazzRequired) {
        return new ContextResolveException("Error while parsing " + context.getCommandUsed()
                + "'s argument input '" + raw + "' , it's NOT any type of " + clazzRequired.getSimpleName());
    }

    public <T> void registerResolver(Class<T> clazz, ValueResolver<C, T> resolver) {
        setData(clazz, resolver);
    }

    public <E extends Enum<E>> void registerEnumResolver(Class<E> enumClass) {
        registerResolver(enumClass, new EnumValueResolver<>(enumClass));
    }

    @SuppressWarnings("unchecked")
    public <T> ValueResolver<C, T> getResolver(Class<T> type) {
        return (ValueResolver<C, T>) getData(TypeUtility.primitiveToBoxed(type)).orElse(null);
    }

}
