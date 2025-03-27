package dev.velix.imperat.command.parameters.type;

import dev.velix.imperat.command.parameters.NumericRange;
import dev.velix.imperat.context.FlagData;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.*;

import java.lang.reflect.Type;
import java.util.List;

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

    public static <S extends Source> ParameterFlag<S> flag(FlagData<S> flagData) {
        return new ParameterFlag<>(flagData);
    }


    public static @NotNull <S extends Source> ParameterCommand<S> command(String name, List<String> aliases) {
        return new ParameterCommand<>(name, aliases);
    }

    public static <S extends Source> ParameterUUID<S> uuid() {
        return new ParameterUUID<>();
    }
}
