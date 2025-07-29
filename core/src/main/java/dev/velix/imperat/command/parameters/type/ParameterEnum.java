package dev.velix.imperat.command.parameters.type;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.parse.InvalidEnumException;
import dev.velix.imperat.util.TypeUtility;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class ParameterEnum<S extends Source> extends BaseParameterType<S, Enum<?>> {

    public ParameterEnum(TypeWrap<Enum<?>> typeWrap) {
        super(typeWrap.getType());
        Class<? extends Enum<?>> type = (Class<? extends Enum<?>>) typeWrap.getType();
        for (var constantEnum : type.getEnumConstants()) {
            suggestions.add(constantEnum.name());
        }
    }

    @Override
    public @NotNull Enum<?> resolve(@NotNull ExecutionContext<S> context, @NotNull CommandInputStream<S> commandInputStream, @NotNull String input) throws ImperatException {

        Type enumType = commandInputStream.currentParameter()
            .filter(param -> TypeUtility.matches(type, Enum.class))
            .map(CommandParameter::valueType)
            .orElse(type);

        try {
            return Enum.valueOf((Class<? extends Enum>) enumType, input);
        } catch (IllegalArgumentException | EnumConstantNotPresentException ex) {
            throw new InvalidEnumException(input, (Class<? extends Enum>) enumType);
        }
    }

    @Override
    public boolean matchesInput(String input, CommandParameter<S> parameter) {
        try {
            if (!TypeWrap.of(type).isSubtypeOf(Enum.class)) {
                return true;
            }
            Enum.valueOf((Class<? extends Enum>) type, input);
            return true;
        } catch (IllegalArgumentException | EnumConstantNotPresentException ex) {
            return false;
        }
    }

}
