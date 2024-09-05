package dev.velix.imperat.annotations;

import dev.velix.imperat.annotations.element.CommandAnnotatedElement;
import dev.velix.imperat.annotations.types.*;
import dev.velix.imperat.annotations.types.Help;
import dev.velix.imperat.annotations.types.Usage;
import dev.velix.imperat.util.collections.ClassMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

@ApiStatus.Internal
public final class AnnotationRegistry {

    private final LinkedHashSet<Class<? extends Annotation>> types = new LinkedHashSet<>();
    private final LinkedHashSet<Class<? extends Annotation>> mains = new LinkedHashSet<>();
    private final ClassMap<Annotation, AnnotationReplacer<?>> replacers = new ClassMap<>();

    public AnnotationRegistry() {
        mains.add(Command.class);
        mains.add(Usage.class);
        mains.add(SubCommand.class);
        mains.add(Help.class);
        
        this.registerAnnotationTypes(Command.class, Description.class, Permission.class);
        this.registerAnnotationTypes(Usage.class, SubCommand.class, Help.class, Command.class);
        this.registerAnnotationTypes(DefaultValue.class, DefaultValueProvider.class,
                Flag.class, Greedy.class, Named.class, Optional.class, Range.class);
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
        types.addAll(Arrays.asList(annotationClasses));
    }
    
    public boolean isMainType(Class<? extends Annotation> type) {
        return isRegistered(type, mains);
    }
    public boolean isRegisteredType(Class<? extends Annotation> annotationClass) {
        return isRegistered(annotationClass, types);
    }
    public boolean isRegistered(Class<? extends Annotation> annotationClass,
                                Collection<Class<? extends Annotation>> annotations) {
        for (Class<? extends Annotation> aC : annotations) {
            if (aC.getName().equals(annotationClass.getName()))
                return true;
        }
        return false;
    }

    public @Nullable Annotation getMainAnnotation(CommandAnnotatedElement<?> element) {
        for(var ann : element.getAnnotations()) {
            if(isMainType(ann.annotationType()))
                return ann;
        }
        return null;
    }
}
