package dev.velix.imperat.annotations.injectors;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.*;
import dev.velix.imperat.annotations.element.CommandAnnotatedElement;
import dev.velix.imperat.annotations.element.MethodParameterElement;
import dev.velix.imperat.annotations.injectors.context.InjectionContext;
import dev.velix.imperat.annotations.injectors.context.ProxyCommand;
import dev.velix.imperat.annotations.types.Named;
import dev.velix.imperat.annotations.types.SubCommand;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public final class SubCommandInjector<S extends Source> extends AnnotationDataInjector<Command<S>, S, SubCommand> {

    public SubCommandInjector(Imperat<S> dispatcher) {
        super(
                dispatcher,
                InjectionContext.of(SubCommand.class, new TypeWrap<Command<S>>() {
                }, AnnotationLevel.METHOD)
        );
    }

    @Override
    public @NotNull <T> Command<S> inject(
            ProxyCommand<S> proxyCommand,
            @Nullable Command<S> toLoad,
            AnnotationReader reader,
            AnnotationParser<S> parser,
            AnnotationRegistry annotationRegistry,
            AnnotationInjectorRegistry<S> injectorRegistry,
            @NotNull CommandAnnotatedElement<?> element,
            @NotNull SubCommand annotation
    ) {

        if (toLoad == null) {
            throw new IllegalArgumentException("toLoad ,in @Description injection, is null.");
        }

        if (element.getElement() instanceof Class) {
            //we will use the injector for @Inherit to load the subcommand
            return toLoad;
        }

        Method method = (Method) element.getElement();

        final String[] values = annotation.value();
        List<String> aliases = new ArrayList<>(Arrays.asList(values)
                .subList(1, values.length));

        var mainUsage = toLoad.getMainUsage();
        List<CommandParameter> methodCommandParameters = this.loadParameters(proxyCommand, injectorRegistry,
                annotationRegistry, reader, parser, mainUsage, method);

        //merging the command's main usage params with subcommand's parameters to use them in the method to be invoked
        List<CommandParameter> fullParams = new ArrayList<>(mainUsage.getParameters().size() + methodCommandParameters.size());
        fullParams.addAll(mainUsage.getParameters());
        fullParams.addAll(methodCommandParameters);
		
		/*UsageCooldown cooldown = loadCooldown(element);
		String desc = element.isAnnotationPresent(Description.class) ? element.getAnnotation(Description.class).value() : "N/A";
		String permission = element.isAnnotationPresent(Permission.class) ? element.getAnnotation(Permission.class).value() : null;
		*/
        var usage = CommandUsage.<S>builder()
                .parameters(methodCommandParameters)
                .execute(new MethodCommandExecutor<>(proxyCommand, dispatcher, method,
                        fullParams)).build();

        //injecting other data into the usage
        UsageInjector.injectOthers(proxyCommand, usage, reader, parser,
                injectorRegistry, annotationRegistry, element);

        toLoad.addSubCommandUsage(values[0], aliases, usage, annotation.attachDirectly());

        return toLoad;
    }

    private List<CommandParameter> loadParameters(
            ProxyCommand<S> proxyCommand,
            AnnotationInjectorRegistry<S> injectorRegistry,
            AnnotationRegistry annotationRegistry,
            AnnotationReader reader,
            AnnotationParser<S> parser,
            @Nullable CommandUsage<S> mainUsage,
            Method method
    ) {

        List<CommandParameter> commandParameters = new ArrayList<>();
        CommandParameterInjector<S> paramInjector = injectorRegistry.<CommandParameter, Named, CommandParameterInjector<S>>
                        getInjector(Named.class, TypeWrap.of(CommandParameter.class), AnnotationLevel.PARAMETER)
                .orElseThrow();

        for (Parameter parameter : method.getParameters()) {
            if (dispatcher.canBeSender(parameter.getType())) continue;
            if (dispatcher.hasContextResolver(parameter.getType())) continue;

            MethodParameterElement element = new MethodParameterElement(annotationRegistry, parameter);
            CommandParameter commandParameter =
                    paramInjector.inject(proxyCommand, null, reader,
                            parser, annotationRegistry,
                            injectorRegistry, element, element.getAnnotation(Named.class));

            if (mainUsage != null && mainUsage
                    .hasParameters((param) -> param.equals(commandParameter))) {
                continue;
            }

            commandParameters.add(commandParameter);
        }
        return commandParameters;
    }
}
