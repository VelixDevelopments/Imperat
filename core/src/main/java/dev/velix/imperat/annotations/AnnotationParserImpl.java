package dev.velix.imperat.annotations;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.element.CommandAnnotatedElement;
import dev.velix.imperat.annotations.injectors.AnnotationInjectorRegistry;
import dev.velix.imperat.annotations.injectors.context.ProxyCommand;
import dev.velix.imperat.annotations.types.Command;
import dev.velix.imperat.annotations.types.Inherit;
import dev.velix.imperat.annotations.types.SubCommand;
import dev.velix.imperat.annotations.types.Usage;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.exceptions.UnknownCommandClass;
import dev.velix.imperat.util.TypeWrap;
import dev.velix.imperat.util.annotations.MethodVerifier;
import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@ApiStatus.Internal
@SuppressWarnings("unchecked")
final class AnnotationParserImpl<S extends Source> extends AnnotationParser<S> {

    private final AnnotationRegistry annotationRegistry;
    private final AnnotationInjectorRegistry<S> injectors;

    private final TypeWrap<dev.velix.imperat.command.Command<S>> commandTypeWrap = new TypeWrap<>() {
    };
    private final TypeWrap<CommandUsage<S>> commandUsageTypeWrap = new TypeWrap<>() {
    };
    AnnotationParserImpl(Imperat<S> dispatcher) {
        super(dispatcher);
        this.annotationRegistry = new AnnotationRegistry();
        this.injectors = AnnotationInjectorRegistry.create(dispatcher);

    }


    /**
     * Parses annotated command class of type {@linkplain T}
     * into {@link Command} then register it using {@link Imperat}
     *
     * @param commandAnnotation the command annotation loaded from the class
     * @param instance          the instance of the command class
     */
    @Override
    public <T> dev.velix.imperat.command.Command<S> parseCommandClass(
            Command commandAnnotation,
            AnnotationReader reader,
            CommandAnnotatedElement<?> element,
            T instance,
            Class<T> instanceClazz
    ) {

        var proxyInjector = injectors.getInjector(Command.class, commandTypeWrap, AnnotationLevel.CLASS).orElseThrow();
        dev.velix.imperat.command.Command<S> commandObject = proxyInjector.inject(null, null, reader, this, annotationRegistry, injectors, element, commandAnnotation);
        ProxyCommand<S> proxyCommand = new ProxyCommand<>(instanceClazz, instance, commandObject);
        //loading class-level annotations
        injectors.injectForElement(
                proxyCommand, reader, this, annotationRegistry,
                injectors, element, AnnotationLevel.CLASS, commandTypeWrap, commandObject
        );

        Set<Method> methods = Arrays.stream(instanceClazz.getDeclaredMethods())
                .sorted((m1, m2) -> getMethodPriority(m1) - getMethodPriority(m2))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (Method method : methods) {
            if (!MethodVerifier.isMethodAcceptable(method)) continue;
            MethodVerifier.verifyMethod(dispatcher, instanceClazz, method);

            var methodKey = AnnotationHelper.getKey(AnnotationLevel.METHOD, method);
            CommandAnnotatedElement<Method> methodElement = (CommandAnnotatedElement<Method>) reader.getAnnotated(AnnotationLevel.METHOD, methodKey);
            assert methodElement != null;
            
            System.out.println("Injecting on method '" + method.getName() + "'");
            //methodElement.debug();
            //loading method-level annotations
            if (methodElement.isAnnotationPresent(Usage.class)) {
                Usage annotation = methodElement.getAnnotation(Usage.class);
                var usageInjector = injectors.getInjector(Usage.class, commandUsageTypeWrap, AnnotationLevel.METHOD);
                CommandUsage<S> usage = usageInjector.orElseThrow().inject(proxyCommand, null, reader, this, annotationRegistry, injectors, methodElement, annotation);
                if (usage.isDefault()) {
                    commandObject.setDefaultUsageExecution(usage.getExecution());
                } else {
                    injectors.injectForElement(
                            proxyCommand, reader, this, annotationRegistry,
                            injectors, methodElement, AnnotationLevel.METHOD, commandUsageTypeWrap, usage
                    );
                    commandObject.addUsage(usage);
                }

            } else if (methodElement.isAnnotationPresent(SubCommand.class)) {
                //TODO get injector for sub command
                SubCommand annotation = methodElement.getAnnotation(SubCommand.class);
                var subCmdMethodInjector = injectors.getInjector(SubCommand.class, commandTypeWrap, AnnotationLevel.METHOD).orElseThrow();
                commandObject = subCmdMethodInjector.inject(proxyCommand, commandObject, reader, this, annotationRegistry, injectors, methodElement, annotation);
            } else if (methodElement.isAnnotationPresent(Command.class)) {
                Command cmdMethodAnnotation = methodElement.getAnnotation(Command.class);
                var cmdMethodInjector = injectors.getInjector(Command.class, commandTypeWrap, AnnotationLevel.METHOD).orElseThrow();

                //Totally different command
                //using dispatcher to register it
                dev.velix.imperat.command.Command<S> internalCmd = cmdMethodInjector.inject(proxyCommand, null, reader, this, annotationRegistry, injectors, methodElement, cmdMethodAnnotation);
                dispatcher.registerCommand(internalCmd);
            }
        }

        //after we loaded the methods of the main root command, let's load the inherited subcommands
        if (element.isAnnotationPresent(Inherit.class)) {
            var inheritanceInjector = injectors.getInjector(Inherit.class, new TypeWrap<dev.velix.imperat.command.Command<S>>() {
            }, AnnotationLevel.CLASS).orElseThrow();
            inheritanceInjector.inject(proxyCommand, commandObject, reader, this, annotationRegistry, injectors, element, element.getAnnotation(Inherit.class));
        }
        return commandObject;
    }

    @Override
    public <T> dev.velix.imperat.command.Command<S> parseCommandClass(T instance) {
        Class<T> instanceClazz = (Class<T>) instance.getClass();
        AnnotationReader reader = AnnotationReader.read(annotationRegistry, instanceClazz);
        var elementKey = AnnotationHelper.getKey(AnnotationLevel.CLASS, instanceClazz);

        CommandAnnotatedElement<Class<?>> element = (CommandAnnotatedElement<Class<?>>) reader.getAnnotated(AnnotationLevel.CLASS, elementKey);
        assert element != null;
        Command commandAnnotation = element.getAnnotation(Command.class);
        if (commandAnnotation == null) {
            throw new UnknownCommandClass(instanceClazz);
        }
         /*for (Class<?> inner : instanceClazz.getDeclaredClasses()) {
            parseCommandClass(inner);
        }*/
        return parseCommandClass(commandAnnotation, reader, element, instance, instanceClazz);

    }


    /**
     * Registers {@link AnnotationReplacer}
     *
     * @param type     the type to replace the annotation by
     * @param replacer the replacer
     */
    @Override
    public <A extends Annotation> void registerAnnotationReplacer(Class<A> type, AnnotationReplacer<A> replacer) {
        annotationRegistry.registerAnnotationReplacer(type, replacer);
    }

    public AnnotationRegistry getRegistry() {
        return annotationRegistry;
    }

    private int getMethodPriority(Method method) {
        int count = method.getParameterCount();
        if (AnnotationHelper.isMethodHelp(method)) {
            count--;
        }

        if (method.isAnnotationPresent(Usage.class)) {
            //if default -> -1 else -> 0;
            return count == 1 ? -1 : 0;
        } else if (method.isAnnotationPresent(SubCommand.class)) {
            return 2 + count;
        }
        return 100;
    }


}
