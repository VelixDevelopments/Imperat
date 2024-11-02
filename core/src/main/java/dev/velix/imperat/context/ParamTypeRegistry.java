package dev.velix.imperat.context;

import dev.velix.imperat.command.parameters.type.ParameterEnum;
import dev.velix.imperat.command.parameters.type.ParameterType;
import dev.velix.imperat.command.parameters.type.ParameterTypes;
import dev.velix.imperat.context.internal.CommandFlag;
import dev.velix.imperat.exception.SourceException;
import dev.velix.imperat.util.Registry;
import dev.velix.imperat.util.TypeUtility;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.UUID;

@ApiStatus.Internal
public final class ParamTypeRegistry<S extends Source> extends Registry<Type, ParameterType<S, ?>> {


    private ParamTypeRegistry() {
        super();
        registerResolver(Boolean.class, ParameterTypes.bool());
        registerResolver(String.class, ParameterTypes.string());
        registerResolver(UUID.class, ParameterTypes.uuid());
        registerResolver(CommandFlag.class, ParameterTypes.flag());

    }

    public static <S extends Source> ParamTypeRegistry<S> createDefault() {
        return new ParamTypeRegistry<>();
    }

    private SourceException exception(String raw,
                                      Class<?> clazzRequired) {
        return new SourceException(
            "Error while parsing argument '%s', It's not a valid %s", raw, clazzRequired.getSimpleName()
        );
    }

    public <T> void registerResolver(Type type, ParameterType<S, T> resolver) {
        if (TypeUtility.areRelatedTypes(type, Enum.class)) return;
        setData(type, resolver);
    }

    @SuppressWarnings("unchecked")
    public Optional<ParameterType<S, ?>> getResolver(Type type) {
        if (TypeUtility.isNumericType(TypeWrap.of(type)))
            return Optional.of(ParameterTypes.numeric((Class<? extends Number>) type));

        //TODO make check for enum
        return Optional.ofNullable(getData(TypeUtility.primitiveToBoxed(type)).orElseGet(() -> {
            if (TypeUtility.areRelatedTypes(type, Enum.class)) {
                ParameterEnum<S> preloadedEnumType = new ParameterEnum<>((TypeWrap<Enum<?>>) TypeWrap.of(type));
                registerResolver(type, preloadedEnumType);
                return preloadedEnumType;
            }

            for (var registeredType : getKeys()) {
                if (TypeUtility.areRelatedTypes(type, registeredType)) {
                    return getData(registeredType).orElse(null);
                }
            }
            return null;
        }));
    }

}
