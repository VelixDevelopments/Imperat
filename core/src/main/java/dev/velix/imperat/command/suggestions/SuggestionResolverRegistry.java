package dev.velix.imperat.command.suggestions;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.FlagParameter;
import dev.velix.imperat.context.CommandFlag;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.SuggestionContext;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.util.Registry;
import dev.velix.imperat.util.TypeUtility;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;

@ApiStatus.Internal
public final class SuggestionResolverRegistry<S extends Source> extends Registry<Type, SuggestionResolver<S, ?>> {
    
    private final Map<String, SuggestionResolver<S, ?>> resolversPerName;
    
    private final EnumSuggestionResolver enumSuggestionResolver = new EnumSuggestionResolver();
    private final FlagSuggestionResolver flagSuggestionResolver = new FlagSuggestionResolver();
    private SuggestionResolverRegistry() {
        super();
        registerResolverForType(SuggestionResolver.plain(Boolean.class, "true", "false"));
        resolversPerName = new HashMap<>();
    }
    
    public static <S extends Source> SuggestionResolverRegistry<S> createDefault() {
        return new SuggestionResolverRegistry<>();
    }
    
    public <T> void registerResolverForType(SuggestionResolver<S, T> suggestionResolver) {
        Type resolverType = suggestionResolver.getType().getType();
        if (TypeUtility.areRelatedTypes(resolverType, Enum.class)) {
            //we don't register enum related resolvers
            enumSuggestionResolver.registerEnumResolver(resolverType);
            return;
        }
        
        if (TypeUtility.areRelatedTypes(resolverType, CommandFlag.class)) {
            return;
        }
        setData(resolverType, suggestionResolver);
    }
    
    public <T> void registerNamedResolver(String name,
                                          SuggestionResolver<S, T> suggestionResolver) {
        resolversPerName.put(name, suggestionResolver);
    }
    
    public @Nullable SuggestionResolver<S, ?> getResolver(Type type) {
        if (TypeUtility.areRelatedTypes(type, Enum.class)) {
            return enumSuggestionResolver;
        }
        
        if (TypeUtility.areRelatedTypes(type, CommandFlag.class)) {
            return flagSuggestionResolver;
        }
        
        return getData(type).orElseGet(() -> {
            for (var resolverByType : this.getAll()) {
                if (resolverByType.getType().isSupertypeOf(type)) {
                    return resolverByType;
                }
            }
            return null;
        });
    }
    
    @SuppressWarnings("unchecked")
    public @Nullable <T> SuggestionResolver<S, T> getResolverByName(String name) {
        return (SuggestionResolver<S, T>) resolversPerName.get(name);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    final class EnumSuggestionResolver implements SuggestionResolver<S, Enum> {
        private final Map<Type, List<String>> PRE_LOADED_ENUMS = new HashMap<>();
        
        public void registerEnumResolver(Type raw) {
            Class<Enum> enumClass = (Class<Enum>) raw;
            PRE_LOADED_ENUMS.computeIfAbsent(raw,
                    (v) -> Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).toList());
        }
        
        private Optional<List<String>> getResults(Type type) {
            return Optional.ofNullable(PRE_LOADED_ENUMS.get(type));
        }
        
        @Override
        public TypeWrap<Enum> getType() {
            return TypeWrap.of(Enum.class);
        }
        
        @Override
        public List<String> autoComplete(SuggestionContext<S> context, CommandParameter<S> parameterToComplete) {
            Type type = parameterToComplete.type();
            return getResults(type)
                    .orElseGet(() -> {
                        registerEnumResolver(type);
                        return getResults(type).orElse(Collections.emptyList());
                    });
        }
    }
    
    final class FlagSuggestionResolver implements SuggestionResolver<S, CommandFlag> {
        
        @Override
        public TypeWrap<CommandFlag> getType() {
            return TypeWrap.of(CommandFlag.class);
        }
        
        @Override
        public Collection<String> autoComplete(SuggestionContext<S> context, CommandParameter<S> parameterToComplete) {
            assert parameterToComplete.isFlag();
            FlagParameter<S> flagParameter = parameterToComplete.asFlagParameter();
            CompletionArg arg = context.getArgToComplete();
            CommandFlag data = flagParameter.getFlagData();
            
            if (flagParameter.isSwitch()) {
                //normal one arg
                return autoCompleteFlagNames(data);
            }
            
            //normal flag with a value next!
            int paramPos = parameterToComplete.position();
            int argPos = arg.index();
            if (argPos > paramPos) {
                //auto-complete the value for the flag
                var flagInputResolver = getResolver(TypeWrap.of(data.inputType()).getType());
                if (flagInputResolver == null)
                    return List.of();
                return flagInputResolver.autoComplete(context, parameterToComplete);
            } else {
                //auto-complete the flag-names
                return autoCompleteFlagNames(data);
            }
            
        }
        
        private List<String> autoCompleteFlagNames(CommandFlag data) {
            List<String> results = new ArrayList<>();
            results.add("-" + data.name());
            for (var alias : data.aliases()) {
                results.add("-" + alias);
            }
            return results;
        }
    }
    
}
