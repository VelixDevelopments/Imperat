package dev.velix.imperat.command.parameters.type;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.SourceException;
import dev.velix.imperat.util.TypeUtility;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class ParameterEnum<S extends Source> extends BaseParameterType<S, Enum<?>> {

    public ParameterEnum(TypeWrap<Enum<?>> typeWrap) {
        super(typeWrap);
        Class<? extends Enum<?>> type = (Class<? extends Enum<?>>) typeWrap.getType();
        for (var constantEnum : type.getEnumConstants()) {
            suggestions.add(constantEnum.name());
        }
    }

    @Override
    public @NotNull Enum<?> resolve(@NotNull ExecutionContext<S> context, @NotNull CommandInputStream<S> commandInputStream, String input) throws ImperatException {

        Type enumType = commandInputStream.currentParameter()
            .filter(param -> TypeUtility.matches(type, Enum.class))
            .map(CommandParameter::valueType)
            .orElse(type);

        var raw = commandInputStream.currentRaw();
        try {
            assert raw.isPresent();
            return Enum.valueOf((Class<? extends Enum>) enumType, raw.get());
        } catch (IllegalArgumentException | EnumConstantNotPresentException ex) {
            throw new SourceException("Invalid " + enumType.getTypeName() + " '" + raw + "'");
        }
    }

    @Override
    public boolean matchesInput(String input, CommandParameter<S> parameter) {
        try {
            if (!TypeWrap.of(type).isSubtypeOf(Enum.class)) {
                return true;
            }
            //ImperatDebugger.debug("type-enum-name= " + type.getTypeName());
            Enum.valueOf((Class<? extends Enum>) type, input);
            return true;
        } catch (IllegalArgumentException | EnumConstantNotPresentException ex) {
            return false;
        }
    }

    @Override
    public @NotNull Enum<?> fromString(Imperat<S> imperat, String input) throws ImperatException {
        return Enum.valueOf((Class<? extends Enum>) type, input);
    }
}
