package dev.velix.imperat.context.internal.sur.handlers;

import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.context.internal.sur.HandleResult;
import org.jetbrains.annotations.NotNull;

public sealed interface ParameterHandler<S extends Source>
        permits CommandParameterHandler, EmptyInputHandler, FlagInputHandler,
        FreeFlagHandler, NonFlagWhenExpectingFlagHandler,
        OptionalParameterHandler, RequiredParameterHandler
{
    
    @NotNull HandleResult handle(ResolvedContext<S> context, CommandInputStream<S> stream);
    
}