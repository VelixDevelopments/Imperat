package dev.velix.imperat.command.suggestions;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.FlagParameter;
import dev.velix.imperat.context.FlagData;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.SuggestionContext;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.resolvers.TypeSuggestionResolver;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;

@ApiStatus.Internal
public final class SuggestionResolverRegistry<S extends Source> {

    private final Map<String, SuggestionResolver<S>> resolversPerName;

    private final EnumSuggestionResolver enumSuggestionResolver = new EnumSuggestionResolver();
    private final FlagSuggestionResolver flagSuggestionResolver = new FlagSuggestionResolver();

    private final Imperat<S> imperat;

    private SuggestionResolverRegistry(Imperat<S> imperat) {
        super();
        this.imperat = imperat;
        resolversPerName = new HashMap<>();
    }

    public static <S extends Source> SuggestionResolverRegistry<S> createDefault(Imperat<S> imperat) {
        return new SuggestionResolverRegistry<>(imperat);
    }

    public FlagSuggestionResolver getFlagSuggestionResolver() {
        return flagSuggestionResolver;
    }

    public EnumSuggestionResolver getEnumSuggestionResolver() {
        return enumSuggestionResolver;
    }

    public void registerNamedResolver(String name,
                                      SuggestionResolver<S> suggestionResolver) {
        resolversPerName.put(name, suggestionResolver);
    }

    public @Nullable SuggestionResolver<S> getResolverByName(String name) {
        return resolversPerName.get(name);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public final class EnumSuggestionResolver implements TypeSuggestionResolver<S, Enum> {
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
            Type type = parameter.valueType();
            return getResults(type)
                .orElseGet(() -> {
                    registerEnumResolver(type);
                    return getResults(type).orElse(Collections.emptyList());
                });
        }
    }

    public final class FlagSuggestionResolver implements TypeSuggestionResolver<S, FlagData> {

        @Override
        public @NotNull TypeWrap<FlagData> getType() {
            return TypeWrap.of(FlagData.class);
        }

        @Override
        public Collection<String> autoComplete(SuggestionContext<S> context, CommandParameter<S> parameter) {
            assert parameter.isFlag();
            FlagParameter<S> flagParameter = parameter.asFlagParameter();
            CompletionArg arg = context.getArgToComplete();
            FlagData data = flagParameter.flagData();

            if (flagParameter.isSwitch()) {
                //normal one arg
                return autoCompleteFlagNames(data);
            }

            //normal flag with a value next!
            int paramPos = parameter.position();
            int argPos = arg.index();
            if (argPos > paramPos) {
                //auto-complete the value for the flag
                var inputType = imperat.getParameterType(data.inputType().type());
                SuggestionResolver<S> flagInputResolver = inputType == null ? null : inputType.getSuggestionResolver();
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

        private List<String> autoCompleteFlagNames(FlagData data) {
            List<String> results = new ArrayList<>();
            results.add("-" + data.name());
            for (var alias : data.aliases()) {
                results.add("-" + alias);
            }
            return results;
        }
    }

}
