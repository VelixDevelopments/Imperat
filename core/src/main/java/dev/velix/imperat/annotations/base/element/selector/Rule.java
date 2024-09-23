package dev.velix.imperat.annotations.base.element.selector;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.base.AnnotationRegistry;
import dev.velix.imperat.annotations.base.element.MethodElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

@SuppressWarnings("rawtypes")
public sealed interface Rule<T> permits Rule.SimpleRule {
    
    @NotNull RuleCondition<T> condition();
    
    default boolean test(Imperat imperat, AnnotationRegistry registry, T variable) {
        return condition().test(imperat, registry, variable);
    }
    
    void onFailure(AnnotationRegistry registry, T variable);
    
    @NotNull Rule<T> and(@Nullable Rule<T> other);
    
    @NotNull Rule<T> or(@Nullable Rule<T> other);
    
    static <T> Builder<T> builder() {
        return new Builder<>();
    }
    
    static Builder<MethodElement> buildForMethod() {
        return new Builder<>();
    }
    
    class Builder<T> {
        
        RuleCondition<T> condition;
        BiConsumer<AnnotationRegistry, T> runnable;
        
        Builder() {
        
        }
        
        public Builder<T> condition(RuleCondition<T> condition) {
            this.condition = condition;
            return this;
        }
        
        public Builder<T> failure(BiConsumer<AnnotationRegistry, T> runnable) {
            this.runnable = runnable;
            return this;
        }
        
        public Rule<T> build() {
            return new SimpleRule<>(condition, runnable);
        }
        
    }
    
    final class SimpleRule<T> implements Rule<T> {
        private @NotNull RuleCondition<T> condition;
        private final @NotNull BiConsumer<AnnotationRegistry, T> onFailure;
        
        public SimpleRule(@NotNull RuleCondition<T> condition, @NotNull BiConsumer<AnnotationRegistry, T> onFailure) {
            this.condition = condition;
            this.onFailure = onFailure;
        }
        
        @Override
        public void onFailure(AnnotationRegistry registry, T variable) {
            onFailure.accept(registry, variable);
        }
        
        @Override
        public @NotNull Rule<T> and(@Nullable Rule<T> other) {
            if (other == null) return this;
            condition = condition.and(other.condition());
            return this;
        }
        
        @Override
        public @NotNull Rule<T> or(@Nullable Rule<T> other) {
            if (other == null) return this;
            condition = condition.or(other.condition());
            return this;
        }
        
        @Override
        public @NotNull RuleCondition<T> condition() {
            return condition;
        }
        
    }
}