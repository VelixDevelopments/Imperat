package dev.velix.imperat.annotations.injectors;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.*;
import dev.velix.imperat.annotations.element.CommandAnnotatedElement;
import dev.velix.imperat.annotations.element.MethodParameterElement;
import dev.velix.imperat.annotations.injectors.context.InjectionContext;
import dev.velix.imperat.annotations.injectors.context.ProxyCommand;
import dev.velix.imperat.annotations.parameters.AnnotationParameterDecorator;
import dev.velix.imperat.annotations.parameters.NumericParameterDecorator;
import dev.velix.imperat.annotations.types.*;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.NumericRange;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.supplier.OptionalValueSupplier;
import dev.velix.imperat.util.TypeUtility;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.velix.imperat.annotations.injectors.AnnotationInjectorRegistry.logError;

@SuppressWarnings({"unchecked", "unused"})
final class CommandParameterInjector<S extends Source> extends AnnotationDataInjector<CommandParameter, S, Named> {

    private final Method method;

    public CommandParameterInjector(
            Imperat<S> imperat,
            Method proxyMethod
    ) {
        super(
                imperat,
                InjectionContext.of(Named.class, TypeWrap.of(CommandParameter.class), AnnotationLevel.PARAMETER)
        );
        this.method = proxyMethod;
    }

    @Override
    public <T> @NotNull CommandParameter inject(
            ProxyCommand<S> proxyCommand,
            @Nullable CommandParameter toLoad,
            AnnotationReader reader,
            AnnotationParser<S> parser,
            AnnotationRegistry annotationRegistry,
            AnnotationInjectorRegistry<S> registry,
            @NotNull CommandAnnotatedElement<?> paramElement,
            @NotNull Named annotation
    ) {

        MethodParameterElement element = (MethodParameterElement) paramElement;
        Parameter parameter = element.getElement();

        Named named = parameter.getAnnotation(Named.class);
        Flag flag = parameter.getAnnotation(Flag.class);
        Switch switchAnnotation = parameter.getAnnotation(Switch.class);

        if (flag != null && switchAnnotation != null) {
            logError(method, proxyCommand.proxyClass(), "both @Flag and @Switch at the same time !");
            return null;
        }

        String name = AnnotationHelper.getParamName(parameter, named, flag, switchAnnotation);
        boolean optional = flag != null || switchAnnotation != null
                || element.isAnnotationPresent(Optional.class);

        //reading suggestion annotation
        Suggest suggestAnnotation = element.getAnnotation(Suggest.class);
        SuggestionProvider suggestionProvider = element.getAnnotation(SuggestionProvider.class);

        SuggestionResolver<S, T> suggestionResolver = null;

        if (suggestAnnotation != null) {
            suggestionResolver = (SuggestionResolver<S, T>) SuggestionResolver.plain(parameter.getType(), suggestAnnotation.value());
        } else if (suggestionProvider != null) {
            suggestionResolver = dispatcher.getNamedSuggestionResolver(suggestionProvider.value().toLowerCase());
        }

        boolean greedy = parameter.getAnnotation(Greedy.class) != null;

        if (greedy && parameter.getType() != String.class) {
            throw new IllegalArgumentException("Argument '" + parameter.getName() + "' is greedy while having a non-greedy type '" + parameter.getType().getName() + "'");
        }

        dev.velix.imperat.command.Description desc = dev.velix.imperat.command.Description.EMPTY;
        if (element.isAnnotationPresent(Description.class)) {
            desc = dev.velix.imperat.command.Description
                    .of(element.getAnnotation(Description.class).value());
        }

        String permission = null;
        if (element.isAnnotationPresent(Permission.class)) {
            permission = element.getAnnotation(Permission.class).value();
        }

        OptionalValueSupplier<T> optionalValueSupplier = null;
        if (optional) {
            DefaultValue defaultValueAnnotation = parameter.getAnnotation(DefaultValue.class);
            DefaultValueProvider provider = parameter.getAnnotation(DefaultValueProvider.class);
            optionalValueSupplier = AnnotationHelper.deduceOptionalValueSupplier(parameter, defaultValueAnnotation, provider);
        }

        if (flag != null) {
            String[] flagAliases = flag.value();
            return AnnotationParameterDecorator.decorate(
                    CommandParameter.<S, T>flag(name, (Class<T>) flag.inputType())
                            .aliases(getAllExceptFirst(flagAliases))
                            .flagDefaultInputValue(optionalValueSupplier)
                            .description(desc)
                            .permission(permission)
                            .build(),
                    element
            );
        } else if (switchAnnotation != null) {
            String[] switchAliases = switchAnnotation.value();
            return AnnotationParameterDecorator.decorate(
                    CommandParameter.<S>flagSwitch(name)
                            .aliases(getAllExceptFirst(switchAliases))
                            .description(desc)
                            .permission(permission)
                            .build(),
                    element
            );
        }

        CommandParameter param =
                AnnotationParameterDecorator.decorate(
                        CommandParameter.of(
                                name, TypeWrap.of((Class<T>) parameter.getType()), permission, desc,
                                optional, greedy, optionalValueSupplier, suggestionResolver
                        ), element
                );

        if (TypeUtility.isNumericType(TypeWrap.of(param.getType()))
                && element.isAnnotationPresent(Range.class)) {
            Range range = element.getAnnotation(Range.class);
            param = NumericParameterDecorator.decorate(
                    param, NumericRange.of(range.min(), range.max())
            );
        }

        return param;
    }


    private List<String> getAllExceptFirst(String[] array) {
        List<String> flagAliases = new ArrayList<>(array.length - 1);
        flagAliases.addAll(Arrays.asList(array).subList(1, array.length));
        return flagAliases;
    }

}
