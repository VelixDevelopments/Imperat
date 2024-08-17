package dev.velix.imperat.util.annotations;

import java.lang.annotation.Annotation;
import java.util.HashMap;

public final class AnnotationMap
				extends HashMap<Class<? extends Annotation>, Annotation> {
	
	
	public void set(Annotation annotation) {
		this.put(annotation.annotationType(), annotation);
	}
	
}