package dev.velix.imperat.context.internal.sur;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.context.internal.sur.handlers.*;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.util.ImperatDebugger;

@SuppressWarnings("unchecked")
public final class ParameterValueAssigner<S extends Source> {
    
    private static final ParameterChain<?> DEFAULT_CHAIN = createDefaultChainWithFreeFlagHandler();
    
    private final CommandUsage<S> usage;
    private final ResolvedContext<S> context;
    private final CommandInputStream<S> stream;
    private final ParameterChain<S> chain;

    ParameterValueAssigner(Command<S> command, ResolvedContext<S> context, CommandUsage<S> usage) {
        this(command, context, usage, (ParameterChain<S>) DEFAULT_CHAIN);
    }
    
    ParameterValueAssigner(Command<S> command, ResolvedContext<S> context, CommandUsage<S> usage, ParameterChain<S> customChain) {
        this.context = context;
        this.usage = usage;
        this.chain = customChain;
        this.stream = CommandInputStream.of(context.arguments(), usage);
        context.setLastCommand(command);
    }

    private static <S extends Source> ParameterChain<S> createDefaultChainWithFreeFlagHandler() {
        return ChainFactory.<S>builder()
            .withHandler(new EmptyInputHandler<>())
            .withHandler(new CommandParameterHandler<>())
            .withHandler(new FlagInputHandler<>())
            .withHandler(new NonFlagWhenExpectingFlagHandler<>())
            .withHandler(new RequiredParameterHandler<>())
            .withHandler(new OptionalParameterHandler<>())
            .withHandler(new FreeFlagHandler<>())
            .build();
    }

    public static <S extends Source> ParameterValueAssigner<S> create(
        Command<S> command,
        ResolvedContext<S> context,
        CommandUsage<S> usage
    ) {
        return new ParameterValueAssigner<>(command, context, usage);
    }
    
    public static <S extends Source> ParameterValueAssigner<S> createWithCustomChain(
        Command<S> command,
        ResolvedContext<S> context,
        CommandUsage<S> usage,
        ParameterChain<S> customChain
    ) {
        return new ParameterValueAssigner<>(command, context, usage, customChain);
    }

    public void resolve() throws ImperatException {
        ImperatDebugger.debug("Resolving input '%s'", stream.getRawQueue().toString());
        ImperatDebugger.debug("Resolving input stream of usage '%s'", CommandUsage.format(context.getLastUsedCommand().name(), usage));
        chain.execute(context, stream);
    }

    public Command<S> getCommand() {
        return context.getLastUsedCommand();
    }
}