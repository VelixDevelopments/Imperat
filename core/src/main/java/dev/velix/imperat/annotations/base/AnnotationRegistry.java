package dev.velix.imperat.annotations.base;

import dev.velix.imperat.annotations.*;
import dev.velix.imperat.annotations.ExternalSubCommand;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class AnnotationRegistry {

    private final Set<Class<? extends Annotation>> knownAnnotations = new LinkedHashSet<>();

    private final Map<Class<? extends Annotation>, AnnotationReplacer<?>> replacers = new HashMap<>();

    AnnotationRegistry() {
        this.registerAnnotationTypes(
            Command.class, ExternalSubCommand.class, Usage.class, SubCommand.class,
            Cooldown.class, Description.class, Permission.class,
            Suggest.class, SuggestionProvider.class, Default.class, DefaultProvider.class, Values.class,
            Switch.class, Flag.class, Greedy.class, Named.class, Optional.class, ContextResolved.class, Range.class, Async.class,
            PostProcessor.class, PreProcessor.class, Help.class, GlobalAttachmentMode.class
        );
    }

    <A extends Annotation> void registerAnnotationReplacer(Class<A> type, AnnotationReplacer<A> replacer) {
        this.replacers.put(type, replacer);
    }

    @SuppressWarnings("unchecked")
    <A extends Annotation> @Nullable AnnotationReplacer<A> getAnnotationReplacer(Class<A> type) {
        return (AnnotationReplacer<A>) this.replacers.get(type);
    }

    boolean hasReplacerFor(Class<? extends Annotation> clazz) {
        return getAnnotationReplacer(clazz) != null;
    }

    @SafeVarargs
    final void registerAnnotationTypes(Class<? extends Annotation>... annotationClasses) {
        knownAnnotations.addAll(List.of(annotationClasses));
    }

    boolean isRegisteredAnnotation(Class<? extends Annotation> annotationClass) {
        return isRegistered(annotationClass, knownAnnotations);
    }

    private static boolean isRegistered(Class<? extends Annotation> annotationClass,
                                        Collection<Class<? extends Annotation>> annotations) {
        for (Class<? extends Annotation> aC : annotations) {
            if (aC.getName().equals(annotationClass.getName()))
                return true;
        }
        return false;
    }


}
