package dev.velix.imperat.command.suggestions;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.FlagParameter;
import dev.velix.imperat.context.CommandFlag;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.SuggestionContext;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.resolvers.TypeSuggestionResolver;
import dev.velix.imperat.util.Registry;
import dev.velix.imperat.util.TypeUtility;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;

@ApiStatus.Internal
public final class SuggestionResolverRegistry<S extends Source> extends Registry<Type, SuggestionResolver<S>> {
    
    private final Map<String, SuggestionResolver<S>> resolversPerName;
    
    private final EnumSuggestionResolver enumSuggestionResolver = new EnumSuggestionResolver();
    private final FlagSuggestionResolver flagSuggestionResolver = new FlagSuggestionResolver();
    
    private SuggestionResolverRegistry() {
        super();
        registerResolverForType(SuggestionResolver.type(Boolean.class, "true", "false"));
        resolversPerName = new HashMap<>();
    }
    
    public static <S extends Source> SuggestionResolverRegistry<S> createDefault() {
        return new SuggestionResolverRegistry<>();
    }
    
    public <T> void registerResolverForType(TypeSuggestionResolver<S, T> suggestionResolver) {
        this.registerResolverForType(suggestionResolver.getType().getType(), suggestionResolver);
    }
    
    public void registerResolverForType(Type type, SuggestionResolver<S> suggestionResolver) {
        if (TypeUtility.areRelatedTypes(type, Enum.class)) {
            //we preload the enum he registers
            enumSuggestionResolver.registerEnumResolver(type);
        }
        
        setData(type, suggestionResolver);
    }
    
    public void registerNamedResolver(String name,
                                      SuggestionResolver<S> suggestionResolver) {
        resolversPerName.put(name, suggestionResolver);
    }
    
    public @Nullable SuggestionResolver<S> getResolver(Type type) {
        
        return getData(type).orElseGet(() -> {
            
            if (TypeUtility.areRelatedTypes(type, Enum.class)) {
                return enumSuggestionResolver;
            }
            
            if (TypeUtility.areRelatedTypes(type, CommandFlag.class)) {
                return flagSuggestionResolver;
            }
            
            for (var resolverByType : this.getAll()) {
                if (resolverByType instanceof TypeSuggestionResolver<S, ?> typeSuggestionResolver
                        && typeSuggestionResolver.getType().isSupertypeOf(type)) {
                    return resolverByType;
                }
            }
            return null;
        });
    }
    
    public @Nullable SuggestionResolver<S> getResolverByName(String name) {
        return resolversPerName.get(name);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    final class EnumSuggestionResolver implements TypeSuggestionResolver<S, Enum> {
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
        public @NotNull TypeWrap<Enum> getType() {
            return TypeWrap.of(Enum.class);
        }
        
        @Override
        public List<String> autoComplete(SuggestionContext<S> context, CommandParameter<S> parameter) {
            Type type = parameter.type();
            return getResults(type)
                    .orElseGet(() -> {
                        registerEnumResolver(type);
                        return getResults(type).orElse(Collections.emptyList());
                    });
        }
    }
    
    final class FlagSuggestionResolver implements TypeSuggestionResolver<S, CommandFlag> {
        
        @Override
        public @NotNull TypeWrap<CommandFlag> getType() {
            return TypeWrap.of(CommandFlag.class);
        }
        
        @Override
        public Collection<String> autoComplete(SuggestionContext<S> context, CommandParameter<S> parameter) {
            assert parameter.isFlag();
            FlagParameter<S> flagParameter = parameter.asFlagParameter();
            CompletionArg arg = context.getArgToComplete();
            CommandFlag data = flagParameter.flagData();
            
            if (flagParameter.isSwitch()) {
                //normal one arg
                return autoCompleteFlagNames(data);
            }
            
            //normal flag with a value next!
            int paramPos = parameter.position();
            int argPos = arg.index();
            if (argPos > paramPos) {
                //auto-complete the value for the flag
                SuggestionResolver<S> flagInputResolver = getResolver(TypeWrap.of(data.inputType()).getType());
                
                //flag parameter's suggestion resolver is the same resolver for its data input.
                if (flagInputResolver == null)
                    flagInputResolver = flagParameter.inputSuggestionResolver();
                
                if (flagInputResolver == null) return List.of();
                else return flagInputResolver.autoComplete(context, parameter);
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
