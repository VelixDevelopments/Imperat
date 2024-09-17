package dev.velix.context.internal;

import dev.velix.Imperat;
import dev.velix.command.Command;
import dev.velix.command.CommandUsage;
import dev.velix.command.parameters.CommandParameter;
import dev.velix.command.parameters.NumericParameter;
import dev.velix.command.parameters.NumericRange;
import dev.velix.context.*;
import dev.velix.context.internal.sur.SmartUsageResolve;
import dev.velix.exception.ImperatException;
import dev.velix.exception.NumberOutOfRangeException;
import dev.velix.help.CommandHelp;
import dev.velix.resolvers.ContextResolver;
import dev.velix.resolvers.ValueResolver;
import dev.velix.util.TypeUtility;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * The purpose of this class is to resolve string inputs
 * into values , each subcommand {@link Command} may have its own args
 * Therefore, it loads the arguments per subcommand USED in the context input
 * from the command source/sender
 * the results (resolved arguments) will be used later on to
 * find the command usage object then use the found command usage to execute the action.
 *
 * @param <S> the sender type
 */
@ApiStatus.Internal
final class ResolvedContextImpl<S extends Source> extends ContextImpl<S> implements ResolvedContext<S> {
    
    
    private final CommandUsage<S> usage;
    private final FlagRegistry flagRegistry = new FlagRegistry();
    //per command/subcommand because the class 'Command' can be also treated as a sub command
    private final Map<Command<S>, Map<String, ResolvedArgument>> resolvedArgumentsPerCommand = new LinkedHashMap<>();
    //all resolved arguments EXCEPT for subcommands and flags.
    private final Map<String, ResolvedArgument> allResolvedArgs = new LinkedHashMap<>();
    private Command<S> lastCommand;
    
    ResolvedContextImpl(Imperat<S> dispatcher,
                        Context<S> context,
                        CommandUsage<S> usage) {
        super(dispatcher, context.getCommandUsed(), context.getSource(), context.getArguments());
        this.lastCommand = context.getCommandUsed();
        this.usage = usage;
    }
    
    /**
     * Fetches the arguments of a command/subcommand that got resolved
     * except for the arguments that represent the literal/subcommand name arguments
     *
     * @param command the command/subcommand owning the argument
     * @param name    the name of the argument
     * @return the argument resolved from raw into a value
     */
    @Override
    public @Nullable ResolvedArgument getResolvedArgument(Command<S> command, String name) {
        Map<String, ResolvedArgument> resolvedArgs = resolvedArgumentsPerCommand.get(command);
        if (resolvedArgs == null) return null;
        
        return resolvedArgs.get(name);
    }
    
    /**
     * @param command the command/subcommand with certain args
     * @return the command/subcommand's resolved args in as a new array-list
     */
    @Override
    public List<ResolvedArgument> getResolvedArguments(Command<S> command) {
        Map<String, ResolvedArgument> argMap = resolvedArgumentsPerCommand.get(command);
        if (argMap == null) return Collections.emptyList();
        return new ArrayList<>(argMap.values());
    }
    
    /**
     * @return all {@link Command} that have been used in this context
     */
    @Override
    public @NotNull Collection<? extends Command<S>> getCommandsUsed() {
        return resolvedArgumentsPerCommand.keySet();
    }
    
    /**
     * @return an ordered collection of {@link ResolvedArgument} just like how they were entered
     * NOTE: the flags are NOT included as a resolved argument, it's treated differently
     */
    @Override
    public Collection<? extends ResolvedArgument> getResolvedArguments() {
        return allResolvedArgs.values();
    }
    
    /**
     * Fetches a resolved argument's value
     *
     * @param name the name of the command
     * @return the value of the resolved argument
     * @see ResolvedArgument
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable T getArgument(String name) {
        ResolvedArgument argument = allResolvedArgs.get(name);
        if (argument == null) return null;
        return (T) argument.value();
    }
    
    /**
     * Fetches the argument/input resolved by the context
     * using {@link ContextResolver}
     *
     * @param type type of argument to return
     * @return the argument/input resolved by the context
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable T getContextResolvedArgument(Class<T> type) throws ImperatException {
        var factory = dispatcher.getContextResolverFactory();
        if (factory == null) {
            var cr = dispatcher.getContextResolver(type);
            if (cr == null) return null;
            return (T) cr.resolve(this, null);
        }
        ContextResolver<S, T> factoryCr = (ContextResolver<S, T>) factory.create(null);
        return factoryCr == null ? null : factoryCr.resolve(this, null);
    }
    
    @Override
    public CommandHelp createCommandHelp() {
        return new CommandHelp(dispatcher, getCommandUsed(), this);
    }
    
    
    @Override
    public ResolvedFlag getFlag(String flagName) {
        return flagRegistry.getData(flagName).orElse(null);
    }
    
    /**
     * Fetches the flag input value
     * returns null if the flag is a {@link CommandSwitch}
     * OR if the value hasn't been resolved somehow
     *
     * @param flagName the flag name
     * @return the resolved value of the flag input
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable T getFlagValue(String flagName) {
        ResolvedFlag flag = getFlag(flagName);
        if (flag == null) {
            return null;
        }
        return (T) flag.value();
    }
    
    
    /**
     * Resolves the arguments from the given plain input {@link Context}
     */
    @Override
    public void resolve() throws ImperatException {
        if (getArguments().isEmpty())
            return;
        
        SmartUsageResolve<S> handler = SmartUsageResolve.create(getCommandUsed(), usage);
        handler.resolve(dispatcher, this);
        this.lastCommand = handler.getCommand();
    }
    
    
    @Override
    public <T> void resolveArgument(Command<S> command,
                                    @Nullable String raw,
                                    int index,
                                    CommandParameter parameter,
                                    @Nullable T value) throws ImperatException {
        
        if (value != null && TypeUtility.isNumericType(value.getClass())
                && parameter instanceof NumericParameter numericParameter
                && numericParameter.hasRange()
                && !numericParameter.matchesRange((Number) value)) {
            
            NumericRange range = numericParameter.getRange();
            throw new NumberOutOfRangeException(numericParameter, (Number) value, range);
        }
        final ResolvedArgument argument = new ResolvedArgument(raw, parameter, index, value);
        resolvedArgumentsPerCommand.compute(command, (existingCmd, existingResolvedArgs) -> {
            if (existingResolvedArgs != null) {
                existingResolvedArgs.put(parameter.name(), argument);
                return existingResolvedArgs;
            }
            Map<String, ResolvedArgument> args = new LinkedHashMap<>();
            args.put(parameter.name(), argument);
            return args;
        });
        allResolvedArgs.put(parameter.name(), argument);
    }
    
    /**
     * Resolves flag the in the context
     *
     * @param flagRaw        the flag itself raw input
     * @param flagInputRaw   the flag's value if present
     * @param flagInputValue the input value resolved using {@link ValueResolver}
     * @param flagDetected   the optional flag-parameter detected
     */
    @Override
    public void resolveFlag(
            String flagRaw,
            @Nullable String flagInputRaw,
            @Nullable Object flagInputValue,
            CommandFlag flagDetected
    ) {
        flagRegistry.setData(flagDetected.name(),
                new ResolvedFlag(flagDetected, flagRaw, flagInputRaw, flagInputValue));
    }
    
    /**
     * Fetches the last used resolved command
     * of a resolved context!
     *
     * @return the last used command/subcommand
     */
    @Override
    public @NotNull Command<S> getLastUsedCommand() {
        return lastCommand;
    }
    
    /**
     * @return The used usage to use it to resolve commands
     */
    @Override
    public CommandUsage<S> getDetectedUsage() {
        return usage;
    }
    
}
