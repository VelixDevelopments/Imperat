package dev.velix.imperat.annotations;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.element.CommandAnnotatedElement;
import dev.velix.imperat.annotations.element.ElementKey;
import dev.velix.imperat.annotations.element.ElementVisitor;
import dev.velix.imperat.annotations.injectors.AnnotationInjectorRegistry;
import dev.velix.imperat.annotations.injectors.context.ProxyCommand;
import dev.velix.imperat.annotations.types.Command;
import dev.velix.imperat.annotations.types.SubCommand;
import dev.velix.imperat.annotations.types.methods.Usage;
import dev.velix.imperat.exceptions.UnknownCommandClass;
import dev.velix.imperat.util.TypeWrap;
import dev.velix.imperat.util.annotations.MethodVerifier;
import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@ApiStatus.Internal
@SuppressWarnings("unchecked")
final class AnnotationParserImpl<C> extends AnnotationParser<C> {

    private final AnnotationRegistry annotationRegistry;
    private final AnnotationInjectorRegistry<C> dataRegistry;

    AnnotationParserImpl(Imperat<C> dispatcher) {
        super(dispatcher);
        this.annotationRegistry = new AnnotationRegistry();
        this.dataRegistry = AnnotationInjectorRegistry.create(dispatcher);
    }
    
    private <E extends AnnotatedElement> ElementKey getKey(AnnotationLevel level, E element) {
        return ((ElementVisitor<E>) level.getVisitor()).loadKey(element);
    }
    
    /**
     * Parses annotated command class of type {@linkplain T}
     * into {@link Command} then register it using {@link Imperat}
     *
     * @param commandAnnotation the command annotation loaded from the class
     * @param instance          the instance of the command class
     */
    @Override
    public <T> dev.velix.imperat.command.Command<C> parseCommandClass(
            Command commandAnnotation,
            AnnotationReader reader,
            CommandAnnotatedElement<?> element,
            T instance,
            Class<T> instanceClazz
    ) {
        var proxyInjector = dataRegistry.getInjector(Command.class, new TypeWrap<dev.velix.imperat.command.Command<C>>() {}, AnnotationLevel.CLASS).orElseThrow();
        dev.velix.imperat.command.Command<C> commandObject = proxyInjector.inject(null, null, reader, this, annotationRegistry, dataRegistry, element, commandAnnotation);
        
        ProxyCommand<C> proxyCommand = new ProxyCommand<>(instanceClazz, instance, commandObject);
        
        //loading class-level annotations
        dataRegistry.injectForElement(proxyCommand, reader, this, annotationRegistry,
                dataRegistry, element, AnnotationLevel.CLASS);
        
        Set<Method> methods = Arrays.stream(instanceClazz.getDeclaredMethods())
                .sorted((m1, m2) -> getMethodPriority(m1) - getMethodPriority(m2))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        
        for (Method method : methods) {
            if (!MethodVerifier.isMethodAcceptable(method)) continue;
            MethodVerifier.verifyMethod(dispatcher, instanceClazz, method);
            
            var methodKey = getKey(AnnotationLevel.METHOD, method);
            CommandAnnotatedElement<Method> methodElement = (CommandAnnotatedElement<Method>) reader.getAnnotated(AnnotationLevel.METHOD, methodKey);
            assert methodElement != null;
            
            //loading method-level annotations
            dataRegistry.injectForElement(
                    proxyCommand, reader, this, annotationRegistry,
                    dataRegistry, methodElement, AnnotationLevel.METHOD
            );
        }
        return commandObject;
    }

    @Override
    public <T> dev.velix.imperat.command.Command<C> parseCommandClass(T instance) {
        Class<T> instanceClazz = (Class<T>) instance.getClass();
        AnnotationReader reader = AnnotationReader.read(annotationRegistry, instanceClazz);
        var elementKey = getKey(AnnotationLevel.CLASS, instanceClazz);

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
        if(AnnotationHelper.isMethodHelp(method)) {
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
