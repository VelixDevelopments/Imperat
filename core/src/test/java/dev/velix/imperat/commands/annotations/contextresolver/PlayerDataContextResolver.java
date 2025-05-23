package dev.velix.imperat.commands.annotations.contextresolver;

import dev.velix.imperat.components.TestSource;
import dev.velix.imperat.annotations.base.element.ParameterElement;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.resolvers.ContextResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerDataContextResolver implements ContextResolver<TestSource, PlayerData> {

    /**
     * Resolves a parameter's default value
     * if it has been not input by the user
     *
     * @param context   the context
     * @param parameter the parameter (null if used the classic way)
     * @return the resolved default-value
     */
    @Override
    public @Nullable PlayerData resolve(
            @NotNull ExecutionContext<TestSource> context,
            @Nullable ParameterElement parameter
    ) throws ImperatException {
        TestSource source = context.source();
        return new PlayerData(source.name(), source.uuid());
    }

}
