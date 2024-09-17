package dev.velix.util.annotations;

import java.lang.annotation.Annotation;
import java.util.HashMap;

public final class AnnotationMap extends HashMap<Class<? extends Annotation>, Annotation> {
    
    public void set(final Annotation annotation) {
        this.put(annotation.annotationType(), annotation);
    }
    
}
