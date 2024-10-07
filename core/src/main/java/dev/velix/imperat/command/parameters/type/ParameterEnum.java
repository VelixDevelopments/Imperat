package dev.velix.imperat.command.parameters.type;

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
import java.util.Objects;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class ParameterEnum<S extends Source> extends BaseParameterType<S, Enum<?>> {

    ParameterEnum(TypeWrap<Enum<?>> typeWrap) {
        super(typeWrap);
    }

    public ParameterEnum() {
        super(new TypeWrap<>() {
        });
    }

    @Override
    public @NotNull Enum<?> resolve(ExecutionContext<S> context, @NotNull CommandInputStream<S> commandInputStream) throws ImperatException {

        Type enumType = TypeUtility.matches(typeWrap.getType(), Enum.class)
            ? Objects.requireNonNull(commandInputStream.currentParameter()).valueType() : typeWrap.getType();

        var raw = commandInputStream.currentRaw();
        try {
            assert raw != null;
            return Enum.valueOf((Class<? extends Enum>) enumType, raw.toUpperCase());
        } catch (EnumConstantNotPresentException ex) {
            throw new SourceException("Invalid " + enumType.getTypeName() + " '" + raw + "'");
        }
    }

    @Override
    public boolean matchesInput(String input, CommandParameter<S> parameter) {
        try {
            if (!typeWrap.isSubtypeOf(Enum.class)) {
                return true;
            }
            Enum.valueOf((Class<? extends Enum>) typeWrap.getType(), input);
            return true;
        } catch (EnumConstantNotPresentException ex) {
            return false;
        }
    }
}
