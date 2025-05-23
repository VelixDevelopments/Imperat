package dev.velix.imperat.misc;

import dev.velix.imperat.components.TestSource;
import dev.velix.imperat.command.parameters.type.BaseParameterType;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.util.ImperatDebugger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomEnumParameterType extends BaseParameterType<TestSource, CustomEnum> {

    public CustomEnumParameterType() {
        super(CustomEnum.class);
    }

    @Override public @Nullable CustomEnum resolve(
            @NotNull ExecutionContext<TestSource> context,
            @NotNull CommandInputStream<TestSource> inputStream,
            @NotNull String input
    ) throws ImperatException {
        ImperatDebugger.debug("Running resolve for CustomEnumParameterType");
        return CustomEnum.valueOf(input);
    }
}
