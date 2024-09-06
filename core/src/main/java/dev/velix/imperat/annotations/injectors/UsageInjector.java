package dev.velix.imperat.annotations.injectors;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.*;
import dev.velix.imperat.annotations.element.CommandAnnotatedElement;
import dev.velix.imperat.annotations.element.MethodParameterElement;
import dev.velix.imperat.annotations.injectors.context.InjectionContext;
import dev.velix.imperat.annotations.injectors.context.ProxyCommand;
import dev.velix.imperat.annotations.types.Named;
import dev.velix.imperat.annotations.types.Usage;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.help.CommandHelp;
import dev.velix.imperat.help.MethodHelpExecution;
import dev.velix.imperat.util.CommandDebugger;
import dev.velix.imperat.util.Pair;
import dev.velix.imperat.util.TypeUtility;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
final class UsageInjector<S extends Source> extends AnnotationDataInjector<CommandUsage<S>, S, Usage> {

    public UsageInjector(
            Imperat<S> dispatcher
    ) {
        super(
                dispatcher,
                InjectionContext.of(Usage.class, new TypeWrap<CommandUsage<S>>() {
                }, AnnotationLevel.METHOD)
        );
    }

    /*static <S extends Source, A extends Annotation> void injectOthers(
            ProxyCommand<S> proxyCommand,
            CommandUsage<S> usage,
            AnnotationReader reader,
            AnnotationParser<S> parser,
            AnnotationInjectorRegistry<S> injectorRegistry,
            AnnotationRegistry annotationRegistry,
            CommandAnnotatedElement<?> element
    ) {
        for(var inj : injectorRegistry.getAll()) {

        }
        injectorRegistry.forEachInjector((inj) -> !(inj instanceof UsageInjector) && inj.getContext().hasTargetType(new TypeWrap<CommandUsage<S>>() {
        })
                && inj.getContext().isOnLevel(AnnotationLevel.METHOD), (inj) -> {
            A ann = (A) element.getAnnotation(inj.getContext().annClass());
            if (ann != null) {
                AnnotationDataInjector<CommandUsage<S>, S, A> dataInjector = (AnnotationDataInjector<CommandUsage<S>, S, A>) inj;
                dataInjector.inject(proxyCommand, usage, reader, parser, annotationRegistry, injectorRegistry, element, ann);
            }
        });
    }*/

    @Override
    public <T> @NotNull CommandUsage<S> inject(
            ProxyCommand<S> proxyCommand,
            @Nullable CommandUsage<S> toLoad,
            AnnotationReader reader,
            AnnotationParser<S> parser,
            AnnotationRegistry annotationRegistry,
            AnnotationInjectorRegistry<S> injectorRegistry,
            @NotNull CommandAnnotatedElement<?> element,
            @NotNull Usage annotation
    ) {

        Method method = (Method) element.getElement();
        System.out.println("Loading @Usage on method " + method.getName() + " in class " + proxyCommand.proxyClass().getSimpleName());
        
        var parametersInfo = loadParameters(proxyCommand, injectorRegistry, annotationRegistry, reader, parser, method);
        
        final boolean isHelp = parametersInfo.left();
        final List<CommandParameter> params = parametersInfo.right();
        
        CommandDebugger.debugParameters("Loaded method parameters=", parametersInfo.right());

        var execution = isHelp ? new MethodHelpExecution<>(dispatcher, proxyCommand, method, parametersInfo.right())
                : new MethodCommandExecutor<>(proxyCommand, dispatcher, method, params);

        /*if(proxyCommand.proxyClass().isAnnotationPresent(SubCommand.class)) {
            Command<S> cmd = proxyCommand.commandLoaded().getParent();
            while (cmd != null) {
                System.out.println(String.format("Number of params for cmd %s is %s", cmd.getName(), cmd
                        .getMainUsage().getMaxLength()));
                params.removeAll(cmd.getMainUsage().getParameters());
                cmd = cmd.getParent();
            }
        }*/

        //injectOthers(proxyCommand, usage, reader, parser, injectorRegistry, annotationRegistry, element);
        return CommandUsage.<S>builder().parameters(params)
                .execute(execution).build(isHelp);
    }

    private Pair<List<CommandParameter>, Boolean> loadParameters(
            ProxyCommand<S> proxyCommand,
            AnnotationInjectorRegistry<S> injectorRegistry,
            AnnotationRegistry annotationRegistry,
            AnnotationReader reader,
            AnnotationParser<S> parser,
            Method method
    ) {


        List<CommandParameter> commandParameters = new ArrayList<>();
        CommandParameterInjector<S> paramInjector = injectorRegistry.<CommandParameter, Named, CommandParameterInjector<S>>
                        getInjector(Named.class, TypeWrap.of(CommandParameter.class), AnnotationLevel.PARAMETER)
                .orElseThrow();
        boolean help = false;
        for (Parameter parameter : method.getParameters()) {
            if (dispatcher.canBeSender(parameter.getType())) continue;
            if (dispatcher.hasContextResolver(parameter.getType())) continue;
            if (TypeUtility.areRelatedTypes(parameter.getType(), CommandHelp.class)) {
                //CommandHelp parameter
                help = true;
                continue;
            }

            MethodParameterElement element = (MethodParameterElement) reader.getAnnotated(AnnotationLevel.PARAMETER, AnnotationHelper.getKey(AnnotationLevel.PARAMETER, parameter));
            if (element == null) {
                continue;
            }
            System.out.println("Element type= " + parameter.getType().getSimpleName());
            CommandParameter commandParameter =
                    paramInjector.inject(proxyCommand, null, reader,
                            parser, annotationRegistry,
                            injectorRegistry, element, element.getAnnotation(Named.class));
            
            commandParameters.add(commandParameter);
        }
        return new Pair<>(commandParameters, help);
    }

}
