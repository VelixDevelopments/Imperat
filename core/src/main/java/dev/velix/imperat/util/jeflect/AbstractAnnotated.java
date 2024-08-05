package dev.velix.imperat.util.jeflect;

import com.github.romanqed.jfunc.LazySupplier;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

abstract class AbstractAnnotated implements ByteAnnotated {
    private final Supplier<Map<Class<?>, ByteAnnotation>> mapSupplier;

    protected AbstractAnnotated() {
        this.mapSupplier = new LazySupplier<>(this::createMap);
    }

    private Map<Class<?>, ByteAnnotation> createMap() {
        var toMap = getAnnotations();
        var ret = new HashMap<Class<?>, ByteAnnotation>();
        for (var annotation : toMap) {
            ret.put(annotation.getAnnotationClass(), annotation);
        }
        return ret;
    }

    @Override
    public ByteAnnotation getAnnotation(Class<? extends Annotation> annotationClass) {
        return mapSupplier.get().get(annotationClass);
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return mapSupplier.get().containsKey(annotationClass);
    }
}
