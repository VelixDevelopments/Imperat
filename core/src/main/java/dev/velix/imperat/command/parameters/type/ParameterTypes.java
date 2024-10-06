package dev.velix.imperat.command.parameters.type;

import dev.velix.imperat.context.CommandFlag;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.util.Registry;
import dev.velix.imperat.util.TypeUtility;
import dev.velix.imperat.util.TypeWrap;

import java.lang.reflect.Type;

public final class ParameterTypes {

    private final static Registry<Type, ParameterType<?, ?>> types = new Registry<>();

    private ParameterTypes() {
        throw new AssertionError();
    }

    public static <S extends Source> ParameterWord<S> word() {
        return new ParameterWord<>();
    }

    public static <S extends Source> ParameterString<S> string() {
        return new ParameterString<>();
    }

    public static <S extends Source, N extends Number> ParameterNumber<S, N> numeric(Class<N> numType) {
        return ParameterNumber.from(numType);
    }

    public static <S extends Source> ParameterBoolean<S> bool() {
        return new ParameterBoolean<>();
    }

    public static <S extends Source> ParameterFlag<S> flag(CommandFlag flag) {
        return new ParameterFlag<>(flag);
    }

    @SuppressWarnings("unchecked")
    public static <S extends Source, T> ParameterType<S, T> from(TypeWrap<T> typeWrap) {
        Class<T> type = (Class<T>) typeWrap.getType();
        if (TypeUtility.matches(type, String.class)) {
            return (ParameterType<S, T>) string();
        } else if (TypeUtility.matches(type, Boolean.class)) {
            return (ParameterType<S, T>) bool();
        } else if (TypeUtility.matches(type, Integer.class)) {
            return (ParameterType<S, T>) numeric(Integer.class);
        } else if (TypeUtility.matches(type, Long.class)) {
            return (ParameterType<S, T>) numeric(Long.class);
        } else if (TypeUtility.matches(type, Float.class)) {
            return (ParameterType<S, T>) numeric(Float.class);
        } else if (TypeUtility.matches(type, Double.class)) {
            return (ParameterType<S, T>) numeric(Double.class);
        } else {
            throw new IllegalArgumentException("Unsupported parameter type: " + type.getName());
        }
    }

}
