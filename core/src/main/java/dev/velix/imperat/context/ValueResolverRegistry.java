package dev.velix.imperat.context;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.internal.sur.Cursor;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.InvalidUUIDException;
import dev.velix.imperat.exception.SourceException;
import dev.velix.imperat.resolvers.ValueResolver;
import dev.velix.imperat.util.Registry;
import dev.velix.imperat.util.TypeUtility;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Type;
import java.util.UUID;

@ApiStatus.Internal
public final class ValueResolverRegistry<S extends Source> extends Registry<Type, ValueResolver<S, ?>> {

    private final EnumValueResolver enumValueResolver = new EnumValueResolver();

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

        registerResolver(UUID.class, (context, parameter, cursor, raw) -> {
            try {
                return UUID.fromString(raw);
            } catch (IllegalArgumentException ex) {
                throw new InvalidUUIDException(raw);
            }
        });
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
        if (TypeUtility.areRelatedTypes(type, Enum.class)) return;
        setData(type, resolver);
    }

    public ValueResolver<S, ?> getResolver(Type type) {

        return getData(TypeUtility.primitiveToBoxed(type)).orElseGet(() -> {
            if (TypeUtility.areRelatedTypes(type, Enum.class)) {
                return enumValueResolver;
            }

            for (var registeredType : getKeys()) {
                if (TypeUtility.areRelatedTypes(type, registeredType)) {
                    return getData(registeredType).orElse(null);
                }
            }
            return null;
        });
    }

    @ApiStatus.Internal
    @SuppressWarnings({"rawtypes", "unchecked"})
    final class EnumValueResolver implements ValueResolver<S, Enum<?>> {

        @Override
        public Enum<?> resolve(
                ExecutionContext<S> context,
                CommandParameter<S> parameter,
                Cursor<S> cursor,
                String raw
        ) throws ImperatException {
            var enumType = (Class<? extends Enum>) parameter.type();
            try {
                return Enum.valueOf(enumType, raw.toUpperCase());
            } catch (EnumConstantNotPresentException ex) {
                throw new SourceException("Invalid " + enumType.getSimpleName() + " '" + raw + "'");
            }
        }
    }
}
