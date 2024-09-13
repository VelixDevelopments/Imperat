package dev.velix.imperat.exception;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.BaseImperat;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Messages;
import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.context.Source;

import java.util.ArrayList;
import java.util.List;

public final class InvalidSyntaxException extends SelfHandledException {
    @Override
    public <S extends Source> void handle(Imperat<S> imperat, Context<S> context) {
        S source = context.getSource();
        if (!(context instanceof ResolvedContext<S> resolvedContext) || resolvedContext.getDetectedUsage() == null) {
            source.error(
                    Messages.INVALID_SYNTAX_UNKNOWN_USAGE
                            .replace("<raw_args>", context.getArguments().join(" "))
            );
        }
        
        if (!(context instanceof ResolvedContext<S> resolvedContext)) {
            throw new IllegalCallerException("Invalid syntax exception can NOT be Thrown before resolving the context");
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
        source.error(
                (Messages.INVALID_SYNTAX_INCOMPLETE_USAGE + "\n" + BaseImperat.FULL_SYNTAX_PREFIX + Messages.INVALID_SYNTAX_ORIGINAL_USAGE_SHOWCASE)
                        .replace("<required_args>", builder.toString())
                        .replace("<usage>", imperat.commandPrefix()
                                + CommandUsage.format(resolvedContext.getOwningCommand(), usage))
        );
    }
}
