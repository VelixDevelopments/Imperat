package dev.velix.imperat.annotations.injectors;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.AnnotationParser;
import dev.velix.imperat.annotations.AnnotationReader;
import dev.velix.imperat.annotations.AnnotationRegistry;
import dev.velix.imperat.annotations.element.CommandAnnotatedElement;
import dev.velix.imperat.annotations.injectors.context.InjectionContext;
import dev.velix.imperat.annotations.injectors.context.ProxyCommand;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;

/**
 * This class represents actions that are triggered
 * if an annotation is found to be used on an element
 * Most probably the actions will be based on taking information
 * from {@link Annotation} and put it into an existing object
 *
 * @param <O> object to load type
 * @param <S> command-sender type
 * @param <A> annotation to a load type
 */
@SuppressWarnings("unused")
public abstract class AnnotationDataInjector<O, S extends Source, A extends Annotation> {
    
    protected final Imperat<S> dispatcher;
    private final InjectionContext context;
    
    protected AnnotationDataInjector(
            Imperat<S> dispatcher,
            InjectionContext context
    ) {
        this.dispatcher = dispatcher;
        this.context = context;
    }
    
    /**
     * The key for the injection
     *
     * @return the injection key that identifies the context of injection
     */
    public @NotNull InjectionContext getContext() {
        return context;
    }
    
    /**
     * Injects the command-related object using the annotation
     * data that is loaded using {@link AnnotationReader}
     *
     * @param proxyCommand       the proxy command
     * @param toLoad             the object created to be modified and loaded
     * @param reader             the annotation reader
     * @param parser             the annotation parser
     * @param annotationRegistry the annotation registry
     * @param injectorRegistry   the registry for injectors
     * @param element            the element of parameter
     * @param annotation         the annotation to load
     * @param <T>                a type parameter to help in casting
     */
    public abstract <T> @NotNull O inject(
            ProxyCommand<S> proxyCommand,
            @Nullable O toLoad,
            AnnotationReader reader,
            AnnotationParser<S> parser,
            AnnotationRegistry annotationRegistry,
            AnnotationInjectorRegistry<S> injectorRegistry,
            @NotNull CommandAnnotatedElement<?> element,
            @NotNull A annotation
    );
    
    
}
