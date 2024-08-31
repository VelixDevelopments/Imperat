package dev.velix.imperat.caption.premade;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.caption.Caption;
import dev.velix.imperat.caption.CaptionKey;
import dev.velix.imperat.caption.Messages;
import dev.velix.imperat.command.BaseImperat;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.ResolvedContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class InvalidSyntaxCaption<C> implements Caption<C> {
    /**
     * @return the key
     */
    @Override
    public @NotNull CaptionKey getKey() {
        return CaptionKey.INVALID_SYNTAX;
    }

    /**
     * @param dispatcher the dispatcher
     * @param context    the context
     * @param exception  the exception may be null if no exception provided
     * @return The message in the form of a component
     */
    @Override
    public @NotNull String getMessage(
            @NotNull Imperat<C> dispatcher,
            @NotNull Context<C> context,
            @Nullable Exception exception
    ) {
        
        if(!(context instanceof ResolvedContext<C> resolvedContext) || resolvedContext.getDetectedUsage() == null) {
            return Messages.INVALID_SYNTAX_UNKNOWN_USAGE
                    .replace("<raw_args>", context.getArguments().join(" "));
        }
        var usage = resolvedContext.getDetectedUsage();
        final int last = context.getArguments().size() - 1;

        List<CommandParameter> params = new ArrayList<>(usage.getParameters())
                .stream()
                .filter((param) -> !param.isOptional() && param.getPosition() > last)
                .toList();

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            CommandParameter param = params.get(i);
            assert !param.isOptional();
            builder.append(param.format());
            if (i != params.size() - 1)
                builder.append(' ');

        }
        //INCOMPLETE USAGE, AKA MISSING REQUIRED INPUTS
        return (Messages.INVALID_SYNTAX_INCOMPLETE_USAGE + "\n" + BaseImperat.FULL_SYNTAX_PREFIX + Messages.INVALID_SYNTAX_ORIGINAL_USAGE_SHOWCASE)
                .replace("<required_args>", builder.toString())
                .replace("<usage>", dispatcher.commandPrefix()
                        + CommandUsage.format(resolvedContext.getOwningCommand(), usage));

    }
}
