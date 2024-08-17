package dev.velix.imperat.annotations;

import dev.velix.imperat.annotations.element.CommandAnnotatedElement;
import dev.velix.imperat.annotations.element.ElementVisitor;
import dev.velix.imperat.util.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.*;

final class AnnotationReaderImpl implements AnnotationReader {
	
	private final Class<?> clazz;
	private final CommandAnnotationRegistry registry;
	
	private final Registry<AnnotationLevel, AnnotationContainer> containers = new Registry<>(() -> new EnumMap<>(AnnotationLevel.class));
	
	AnnotationReaderImpl(CommandAnnotationRegistry registry,
	                     Class<?> clazz) {
		this.registry = registry;
		this.clazz = clazz;
		read();
	}
	
	@SuppressWarnings("unchecked")
	private <E extends AnnotatedElement> void read() {
		
		for (AnnotationLevel level : AnnotationLevel.values()) {
			AnnotationContainer container = new AnnotationContainer(level);
			Set<CommandAnnotatedElement<E>> elementsOfLevel = (Set<CommandAnnotatedElement<E>>) level.getElements(registry, clazz);
			
			for (CommandAnnotatedElement<E> element : elementsOfLevel) {
				ElementVisitor<E> visitor = (ElementVisitor<E>) level.getVisitor();
				container.accept(visitor, element);
			}
			containers.setData(level, container);
		}
		
	}
	
	
	/**
	 * @return The class target
	 */
	@Override
	public @NotNull Class<?> getTargetClass() {
		return clazz;
	}
	
	/**
	 * Get annotated element
	 * may be a parameter, method or even a class
	 *
	 * @param level the level
	 * @param key   the key of this element
	 * @return the annotated element
	 */
	@Override
	public @Nullable AnnotatedElement getAnnotated(AnnotationLevel level, ElementKey key) {
		if (containers.getData(level).isEmpty()) return null;
		AnnotationContainer container = containers.getData(level).orElse(null);
		if (container == null) return null;
		return container.getData(key).orElse(null);
	}
	
	/**
	 * Fetches all annotations registered within an element
	 *
	 * @param level the level of this element
	 * @param key   the key of this element
	 * @return the annotations added to this element
	 */
	@Override
	public Collection<Annotation> getAnnotations(AnnotationLevel level, ElementKey key) {
		AnnotatedElement element = getAnnotated(level, key);
		if (element == null) return Collections.emptyList();
		return List.of(element.getAnnotations());
	}
	
	/**
	 * Fetches the annotation of an element
	 *
	 * @param key   the element key
	 * @param level the level of the element
	 * @param type  the type of annotation
	 * @return the annotation added to the element
	 */
	@Override
	public <A extends Annotation> @Nullable A getAnnotation(ElementKey key, AnnotationLevel level, Class<A> type) {
		AnnotatedElement element = getAnnotated(level, key);
		if (element == null) return null;
		return element.getAnnotation(type);
	}
}
