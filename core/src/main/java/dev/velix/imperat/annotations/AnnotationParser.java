package dev.velix.imperat.annotations;

import dev.velix.imperat.CommandDispatcher;
import dev.velix.imperat.annotations.element.CommandAnnotatedElement;
import dev.velix.imperat.annotations.element.ElementVisitor;
import dev.velix.imperat.annotations.injectors.AnnotationDataCreator;
import dev.velix.imperat.annotations.injectors.AnnotationDataRegistry;
import dev.velix.imperat.annotations.types.Command;
import dev.velix.imperat.annotations.types.methods.DefaultUsage;
import dev.velix.imperat.annotations.types.methods.Usage;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.exceptions.UnknownCommandClass;
import dev.velix.imperat.util.annotations.MethodVerifier;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@ApiStatus.Internal
@SuppressWarnings("unchecked")
public final class AnnotationParser<C> {
	
	private final CommandDispatcher<C> dispatcher;
	private final CommandAnnotationRegistry annotationRegistry;
	private final AnnotationDataRegistry<C> dataRegistry;
	
	public AnnotationParser(CommandDispatcher<C> dispatcher) {
		this.dispatcher = dispatcher;
		this.annotationRegistry = new CommandAnnotationRegistry();
		this.dataRegistry = new AnnotationDataRegistry<>(annotationRegistry, dispatcher);
	}
	
	private <E extends AnnotatedElement> AnnotationReader.ElementKey getKey(AnnotationLevel level, E element) {
		return ((ElementVisitor<E>) level.getVisitor()).loadKey(element);
	}
	
	public <T> void parseCommandClass(T instance) {
		Class<T> instanceClazz = (Class<T>) instance.getClass();
		AnnotationReader reader = AnnotationReader.read(annotationRegistry, instanceClazz);
		var elementKey = getKey(AnnotationLevel.CLASS, instanceClazz);
		
		CommandAnnotatedElement<Class<?>> element = (CommandAnnotatedElement<Class<?>>) reader.getAnnotated(AnnotationLevel.CLASS, elementKey);
		assert element != null;
		Command commandAnnotation = element.getAnnotation(Command.class);
		if (commandAnnotation == null) {
			throw new UnknownCommandClass(instanceClazz);
		}
		
		AnnotationDataCreator<dev.velix.imperat.command.Command<C>, Command> creator = dataRegistry.getDataCreator(Command.class);
		assert creator != null;
		dev.velix.imperat.command.Command<C> commandObject = creator.create(instanceClazz, commandAnnotation, element);
		dataRegistry.injectForElement(instanceClazz, commandObject, commandObject, element);
		
		Set<Method> methods = Arrays.stream(instanceClazz.getDeclaredMethods()).sorted((m1, m2) -> {
			if (m1.isAnnotationPresent(Usage.class) && m2.isAnnotationPresent(Usage.class)) {
				return 0;
			} else if (m1.isAnnotationPresent(Usage.class)) {
				return -1;
			} else {
				return 1;
			}
		}).collect(Collectors.toCollection(LinkedHashSet::new));
		
		for (Method method : methods) {
			if (!MethodVerifier.isMethodAcceptable(method)) continue;
			MethodVerifier.verifyMethod(dispatcher, instanceClazz, method, method.isAnnotationPresent(DefaultUsage.class));
			
			var methodKey = getKey(AnnotationLevel.METHOD, method);
			CommandAnnotatedElement<Method> methodElement = (CommandAnnotatedElement<Method>) reader.getAnnotated(AnnotationLevel.METHOD, methodKey);
			assert methodElement != null;
			
			if (!methodElement.isAnnotationPresent(Usage.class)) {
				dataRegistry.injectForElement(instanceClazz, commandObject, commandObject, methodElement);
				continue;
			}
			Usage usageAnnotation = methodElement.getAnnotation(Usage.class);
			AnnotationDataCreator<CommandUsage.Builder<C>, Usage> usageDataCreator = dataRegistry.getDataCreator(Usage.class);
			assert usageDataCreator != null;
			CommandUsage.Builder<C> usageBuilder = usageDataCreator.create(instanceClazz, usageAnnotation, methodElement);
			dataRegistry.injectForElement(instanceClazz, commandObject, usageBuilder, methodElement);
			commandObject.addUsage(usageBuilder.build());
		}
		
		dispatcher.registerCommand(commandObject);
		
		for (Class<?> inner : instanceClazz.getDeclaredClasses()) {
			parseCommandClass(inner);
		}
	}
	
	public CommandAnnotationRegistry getRegistry() {
		return annotationRegistry;
	}
	
}
