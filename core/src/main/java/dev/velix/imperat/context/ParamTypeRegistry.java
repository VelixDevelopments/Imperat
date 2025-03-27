package dev.velix.imperat.context;

import dev.velix.imperat.command.parameters.type.ParameterEnum;
import dev.velix.imperat.command.parameters.type.ParameterType;
import dev.velix.imperat.command.parameters.type.ParameterTypes;
import dev.velix.imperat.util.Registry;
import dev.velix.imperat.util.TypeUtility;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.*;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@ApiStatus.Internal
@SuppressWarnings({"rawtypes", "unchecked"})
public final class ParamTypeRegistry<S extends Source> extends Registry<Type, Supplier<ParameterType>> {


    private ParamTypeRegistry() {
        super();
        registerResolver(Boolean.class, ParameterTypes::bool);
        registerResolver(String.class, ParameterTypes::string);
        registerResolver(UUID.class, ParameterTypes::uuid);

    }

    public static <S extends Source> ParamTypeRegistry<S> createDefault() {
        return new ParamTypeRegistry<>();
    }

    public void registerResolver(Type type, Supplier<ParameterType> resolver) {
        if (TypeUtility.areRelatedTypes(type, Enum.class)) return;
        setData(type, resolver);
    }

    public <T> Optional<ParameterType<S, T>> getResolver(Type type) {
        if (TypeUtility.isNumericType(TypeWrap.of(type)))
            return Optional.of((ParameterType<S, T>) ParameterTypes.numeric((Class<? extends Number>) type));

        return Optional.ofNullable(getData(TypeUtility.primitiveToBoxed(type)).map(Supplier::get).orElseGet(() -> {
            if (TypeUtility.areRelatedTypes(type, Enum.class)) {
                ParameterEnum<S> preloadedEnumType = new ParameterEnum<>((TypeWrap<Enum<?>>) TypeWrap.of(type));
                registerResolver(type, ()-> preloadedEnumType);
                return preloadedEnumType;
            }

            for (var registeredType : getKeys()) {
                if (TypeUtility.areRelatedTypes(type, registeredType)) {
                    return getData(registeredType).map((s)-> ((ParameterType<S, T>)s.get()) ).orElse(null);
                }
            }
            return null;
        }));
    }

}
