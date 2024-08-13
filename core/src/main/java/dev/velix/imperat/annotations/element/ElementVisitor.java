package dev.velix.imperat.annotations.element;

import dev.velix.imperat.annotations.AnnotationContainer;
import dev.velix.imperat.annotations.AnnotationReader;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

@FunctionalInterface
public interface ElementVisitor<E extends AnnotatedElement> {

	AnnotationReader.ElementKey loadKey(E element);

	default void visit(AnnotationContainer container, @NotNull E element) {
		container.addElement(loadKey(element), element);
	}

	static ElementVisitor<CommandAnnotatedElement<Class<?>>> classes() {
		return (element)-> new AnnotationReader.ElementKey(element.getElement().getName());
	}

	static ElementVisitor<CommandAnnotatedElement<Method>> methods() {
		return (element)-> new AnnotationReader.ElementKey(element.getElement().getName(),
				  element.getElement().getParameterTypes());
	}

	static ElementVisitor<ParameterCommandElement> parameters() {
		return (param)-> new AnnotationReader.ElementKey(param.getName(),
				  param.getElement().getType());
	}

}
