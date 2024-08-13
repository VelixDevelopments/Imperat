package dev.velix.imperat.annotations;

import dev.velix.imperat.annotations.element.ElementVisitor;
import dev.velix.imperat.util.Registry;

import java.lang.reflect.AnnotatedElement;

public final class AnnotationContainer extends Registry<AnnotationReader.ElementKey, AnnotatedElement> {

	private final AnnotationLevel level;
	//private final AnnotationReplacerRegistry;
	AnnotationContainer(AnnotationLevel level) {
		this.level = level;
	}


	public void addElement(
			  AnnotationReader.ElementKey key,
			  AnnotatedElement element
	) {
		if(!level.matches(element)) {
			throw new IllegalArgumentException("Failed to add element '"
					  + element.getClass().getName() + "' to level=" + level.name());
		}
		this.setData(key, element);
	}

	public void removeElement(AnnotationReader.ElementKey key) {
		this.setData(key, null);
	}

	public <T extends AnnotatedElement> void accept(
			  ElementVisitor<T> elementVisitor,
			  T element
	) {
		elementVisitor.visit(this, element);
	}

}
