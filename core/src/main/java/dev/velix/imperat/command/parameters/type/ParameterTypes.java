package dev.velix.imperat.command.parameters.type;

import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.NotNull;

public final class ParameterTypes {


    private ParameterTypes() {
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

    public static <S extends Source> ParameterFlag<S> flag() {
        return new ParameterFlag<>();
    }


    public static @NotNull <S extends Source> ParameterCommand<S> command() {
        return new ParameterCommand<>();
    }
}
