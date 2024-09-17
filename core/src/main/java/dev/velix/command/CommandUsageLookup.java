package dev.velix.command;

import dev.velix.Imperat;
import dev.velix.command.parameters.CommandParameter;
import dev.velix.context.ArgumentQueue;
import dev.velix.context.Context;
import dev.velix.context.Source;
import lombok.Data;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@ApiStatus.Internal
@Deprecated(forRemoval = true)
public final class CommandUsageLookup<S extends Source> {
    
    private final Imperat<S> dispatcher;
    private final List<CommandUsage<S>> usages;
    
    CommandUsageLookup(Imperat<S> dispatcher,
                       Command<S> command) {
        this.dispatcher = dispatcher;
        this.usages = new ArrayList<>(command.getUsages());
        //usages.sort(UsageComparator.getInstance());
    }
    
    
    public SearchResult searchUsage(Context<S> context) {
        for (CommandUsage<S> commandUsage : this.usages) {
            if (usageMatchesContext(context, commandUsage))
                return new SearchResult(commandUsage, Result.FOUND_COMPLETE);
            else if (commandUsage.hasParamType(Command.class) && checkResolvedLogic(context, commandUsage))
                return new SearchResult(commandUsage, Result.FOUND_INCOMPLETE);
        }
        
        return new SearchResult(null, Result.NOT_FOUND);
    }
    
    public List<CommandUsage<S>> findUsages(Predicate<CommandUsage<S>> predicate) {
        List<CommandUsage<S>> usages = new ArrayList<>();
        for (CommandUsage<S> usage : this.usages) {
            if (predicate.test(usage)) {
                usages.add(usage);
            }
        }
        return usages;
    }
    
    private boolean usageMatchesContext(Context<S> context, CommandUsage<S> usage) {
        //1-arguments length check from both sides (raw and resolved)
        //2- compare raw and resolved parameters
        return checkLength(context.getArguments(), usage)
                && checkResolvedLogic(context, usage);
    }
    
    @SuppressWarnings("unchecked")
    private boolean checkResolvedLogic(Context<S> context,
                                       CommandUsage<S> usage) {
        
        ArgumentQueue rawArgs = context.getArguments().copy();
        
        int i = 0;
        while (!rawArgs.isEmpty()) {
            if (i >= usage.getMaxLength()) break;
            
            final String raw = rawArgs.poll();
            final CommandParameter parameter = usage.getParameter(i);
            if (parameter == null) break;
            
            if (parameter.isFlag())
                continue;
            
            if (parameter.isCommand()) {
                //the raw is the commandName
                Command<S> sub = (Command<S>) parameter;
                if (!sub.hasName(raw)) {
                    return false;
                }
                
            }
            
            i++;
        }
        
        return true;
    }
    
    private boolean checkLength(ArgumentQueue rawArgs, CommandUsage<S> usage) {
        int rawLength = rawArgs.size();
        
        int maxExpectedLength = usage.getMaxLength();
        int minExpectedLength = usage.getMinLength();
        
        CommandParameter lastParameter = usage.getParameters().get(maxExpectedLength - 1);
        if (lastParameter.isGreedy()) {
            final int minMaxDiff = maxExpectedLength - minExpectedLength;
            int paramPos = lastParameter.position() - minMaxDiff;
            rawLength = rawLength - (rawLength - paramPos - 1);
        }
        
        if (rawLength < minExpectedLength) {
            for (var param : usage.getParameters()) {
                if (param.isOptional()) continue;
                if (rawLength == minExpectedLength) break;
                
                if (dispatcher.getContextResolver(param) != null)
                    rawLength++;
            }
        }
        
        return rawLength >= minExpectedLength && rawLength <= maxExpectedLength;
        
    }
    
    public enum Result {
        
        NOT_FOUND,
        
        FOUND_INCOMPLETE,
        
        FOUND_COMPLETE
        
    }
    
    @Data
    public static final class SearchResult {
        
        private final CommandUsage<?> commandUsage;
        private final Result result;
        
        public static SearchResult of(CommandUsage<?> usage, Result result) {
            return new SearchResult(usage, result);
        }
    }
}
