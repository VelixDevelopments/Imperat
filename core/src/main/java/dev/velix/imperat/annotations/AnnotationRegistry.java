package dev.velix.imperat.annotations;

import dev.velix.imperat.annotations.element.ParseElement;
import dev.velix.imperat.annotations.types.*;
import dev.velix.imperat.util.collections.ClassMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

@ApiStatus.Internal
public final class AnnotationRegistry {
    
    private final LinkedHashSet<Class<? extends Annotation>> metas = new LinkedHashSet<>();
    private final LinkedHashSet<Class<? extends Annotation>> mains = new LinkedHashSet<>();
    private final ClassMap<Annotation, AnnotationReplacer<?>> replacers = new ClassMap<>();
    
    public AnnotationRegistry() {
        mains.add(Command.class);
        mains.add(Inherit.class);
        mains.add(Usage.class);
        mains.add(SubCommand.class);
        
        this.registerAnnotationTypes(
                Cooldown.class, Description.class, Permission.class,
                Suggest.class, SuggestionProvider.class, DefaultValue.class, DefaultValueProvider.class,
                Switch.class, Flag.class, Greedy.class, Named.class, Optional.class, Range.class, Async.class, Cooldown.class
        );
    }
    
    public <A extends Annotation> void registerAnnotationReplacer(Class<A> type, AnnotationReplacer<A> replacer) {
        this.replacers.put(type, replacer);
    }
    
    @SuppressWarnings("unchecked")
    public <A extends Annotation> @Nullable AnnotationReplacer<A> getAnnotationReplacer(Class<A> type) {
        return (AnnotationReplacer<A>) this.replacers.get(type);
    }
    
    public boolean hasReplacerFor(Class<? extends Annotation> clazz) {
        return getAnnotationReplacer(clazz) != null;
    }
    
    @SafeVarargs
    public final void registerAnnotationTypes(Class<? extends Annotation>... annotationClasses) {
        metas.addAll(Arrays.asList(annotationClasses));
    }
    
    public boolean isMainType(Class<? extends Annotation> type) {
        return isRegistered(type, mains);
    }
    
    public boolean isRegisteredMeta(Class<? extends Annotation> annotationClass) {
        return isRegistered(annotationClass, metas);
    }
    
    private boolean isRegistered(Class<? extends Annotation> annotationClass,
                                 Collection<Class<? extends Annotation>> annotations) {
        for (Class<? extends Annotation> aC : annotations) {
            if (aC.getName().equals(annotationClass.getName()))
                return true;
        }
        return false;
    }
    
    public @Nullable Annotation getMainAnnotation(ParseElement<?> element) {
        for (Annotation ann : element.getDeclaredAnnotations()) {
            if (isMainType(ann.annotationType()))
                return ann;
        }
        return null;
    }
    
    public <A extends Annotation> boolean isRegisteredAnnotation(Class<A> clazz) {
        return isMainType(clazz) || isRegisteredMeta(clazz);
    }
}
