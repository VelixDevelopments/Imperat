package dev.velix.imperat.annotations.injectors.context;

import dev.velix.imperat.annotations.AnnotationLevel;
import dev.velix.imperat.util.TypeUtility;
import dev.velix.imperat.util.TypeWrap;

import java.lang.annotation.Annotation;
import java.util.Objects;

public record InjectionContext(
        Class<? extends Annotation> annClass,
        TypeWrap<?> targetToLoad,
        AnnotationLevel level
) {

    public static InjectionContext of(
            Class<? extends Annotation> annClass,
            TypeWrap<?> target,
            AnnotationLevel level
    ) {
        return new InjectionContext(annClass, target, level);
    }

    public boolean hasAnnotationType(Class<? extends Annotation> annClass) {
        return TypeUtility.matches(this.annClass, annClass);
    }

    public boolean hasTargetType(TypeWrap<?> target) {
        return TypeUtility.areRelatedTypes(this.targetToLoad.getType(), target.getType());
    }

    public boolean isOnLevel(AnnotationLevel level) {
        return this.level == level;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InjectionContext that)) return false;
        return level == that.level &&
                Objects.equals(targetToLoad, that.targetToLoad)
                && Objects.equals(annClass, that.annClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(annClass, targetToLoad, level);
    }
}
