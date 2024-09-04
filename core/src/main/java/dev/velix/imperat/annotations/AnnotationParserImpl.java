package dev.velix.imperat.annotations;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.element.CommandAnnotatedElement;
import dev.velix.imperat.annotations.element.ElementKey;
import dev.velix.imperat.annotations.element.ElementVisitor;
import dev.velix.imperat.annotations.types.classes.Command;
import dev.velix.imperat.annotations.types.methods.DefaultUsage;
import dev.velix.imperat.annotations.types.methods.Help;
import dev.velix.imperat.annotations.types.SubCommand;
import dev.velix.imperat.annotations.types.methods.Usage;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.exceptions.UnknownCommandClass;
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
    private final AnnotationHandlerRegistry<C> dataRegistry;

    AnnotationParserImpl(Imperat<C> dispatcher) {
        super(dispatcher);
        this.annotationRegistry = new AnnotationRegistry();
        this.dataRegistry = new AnnotationHandlerRegistry<>(this, annotationRegistry, dispatcher);
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
        AnnotationDataCreator<dev.velix.imperat.command.Command<C>, Command> creator = dataRegistry.getDataCreator(Command.class);
        assert creator != null;
        dev.velix.imperat.command.Command<C> commandObject = creator.create(instance, instanceClazz, commandAnnotation, element);
        dataRegistry.injectForElement(instance, instanceClazz, commandObject, commandObject, reader, element);
        
        Set<Method> methods = Arrays.stream(instanceClazz.getDeclaredMethods())
                .sorted((m1, m2) -> getMethodPriority(m1) - getMethodPriority(m2))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        
        for (Method method : methods) {
            if (!MethodVerifier.isMethodAcceptable(method)) continue;
            MethodVerifier.verifyMethod(dispatcher, instanceClazz, method, method.isAnnotationPresent(DefaultUsage.class));
            
            var methodKey = getKey(AnnotationLevel.METHOD, method);
            CommandAnnotatedElement<Method> methodElement = (CommandAnnotatedElement<Method>) reader.getAnnotated(AnnotationLevel.METHOD, methodKey);
            assert methodElement != null;
            
            if (!methodElement.isAnnotationPresent(Usage.class)) {
                dataRegistry.injectForElement(instance, instanceClazz, commandObject, commandObject, reader, methodElement);
                continue;
            }
            Usage usageAnnotation = methodElement.getAnnotation(Usage.class);
            AnnotationDataCreator<CommandUsage.Builder<C>, Usage> usageDataCreator = dataRegistry.getDataCreator(Usage.class);
            assert usageDataCreator != null;
            CommandUsage.Builder<C> usageBuilder = usageDataCreator.create(instance, instanceClazz, usageAnnotation, methodElement);
            dataRegistry.injectForElement(instance, instanceClazz, commandObject, usageBuilder, reader, methodElement);
            commandObject.addUsage(usageBuilder.build());
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
        if (method.isAnnotationPresent(DefaultUsage.class)) {
            return -1;
        } else if (method.isAnnotationPresent(Usage.class)) {
            return 0;
        } else if (method.isAnnotationPresent(Help.class)) {
            return 1;
        } else if (method.isAnnotationPresent(SubCommand.class)) {
            return 2 + method.getParameterCount();
        }
        return 10;
    }

}
