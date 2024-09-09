package dev.velix.imperat.annotations;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.element.CommandClassVisitor;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.Annotation;

@ApiStatus.Internal
@SuppressWarnings("unchecked")
final class AnnotationParserImpl<S extends Source> extends AnnotationParser<S> {

    private final AnnotationRegistry annotationRegistry;
    
    private final CommandClassVisitor<S> visitor;
    
    AnnotationParserImpl(Imperat<S> dispatcher) {
        super(dispatcher);
        this.annotationRegistry = new AnnotationRegistry();
        this.visitor = CommandClassVisitor.newSimpleVisitor(dispatcher);
    }
    
    
    @Override
    public <T> void parseCommandClass(T instance) {
        Class<T> instanceClazz = (Class<T>) instance.getClass();
        AnnotationReader<S> reader = AnnotationReader.read(annotationRegistry, instanceClazz);
        reader.accept(dispatcher, visitor);
    }


    /**
     * Registers {@link AnnotationReplacer}
     *
     * @param type     the type to replace the annotation by
     * @param replacer the replacer
     */
    @Override
    public <A extends Annotation> void registerAnnotationReplacer(Class<A> type, AnnotationReplacer<A> replacer) {
        annotationRegistry.registerAnnotationReplacer(type, replacer);
    }

    public AnnotationRegistry getRegistry() {
        return annotationRegistry;
    }


}
