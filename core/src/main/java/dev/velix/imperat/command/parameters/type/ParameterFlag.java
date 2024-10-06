package dev.velix.imperat.command.parameters.type;

import dev.velix.imperat.context.CommandFlag;
import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.sur.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.util.Patterns;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParameterFlag<S extends Source> extends BaseParameterType<S, CommandFlag> {
    private final CommandFlag flag;

    protected ParameterFlag(CommandFlag flag) {
        super(TypeWrap.of(CommandFlag.class));
        this.flag = flag;
    }


    //TODO fix fucked up flags structure next week
    @Override
    public @Nullable CommandFlag resolve(ResolvedContext<S> context, @NotNull CommandInputStream<S> commandInputStream) throws ImperatException {
        return null;
    }

    @Override
    public boolean matchesInput(String input) {
        int subStringIndex;
        if (Patterns.SINGLE_FLAG.matcher(input).matches()) {
            subStringIndex = 1;
        } else if (Patterns.DOUBLE_FLAG.matcher(input).matches()) {
            subStringIndex = 2;
        } else {
            subStringIndex = 0;
        }
        String flagInput = input.substring(subStringIndex);
        return flag.acceptsInput(flagInput);
    }
}
