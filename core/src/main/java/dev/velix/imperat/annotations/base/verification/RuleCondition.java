package dev.velix.imperat.annotations.base.verification;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.base.AnnotationRegistry;

@FunctionalInterface
@SuppressWarnings("rawtypes")
public interface RuleCondition<E> {
    boolean test(Imperat imperat, AnnotationRegistry annotationRegistry, E element);
    
    default RuleCondition<E> and(RuleCondition<E> other) {
        if (other == null) return this;
        return (imperat, registry, element) -> this.test(imperat, registry, element)
                && other.test(imperat, registry, element);
    }
    
    default RuleCondition<E> or(RuleCondition<E> other) {
        if (other == null) return this;
        return (imperat, registry, element) -> this.test(imperat, registry, element)
                || other.test(imperat, registry, element);
        
    }
}
