package dev.velix.imperat.annotations.base;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.base.element.CommandClassVisitor;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.Annotation;

@ApiStatus.Internal
final class AnnotationParserImpl<S extends Source> extends AnnotationParser<S> {
    
    private final AnnotationRegistry annotationRegistry;
    
    private final CommandClassVisitor<S> visitor;
    
    AnnotationParserImpl(Imperat<S> dispatcher, CommandClassVisitor<S> visitor) {
        super(dispatcher);
        this.annotationRegistry = new AnnotationRegistry();
        this.visitor = visitor;
    }
    
    
    @Override
    public <T> void parseCommandClass(T instance) {
        //System.out.println("-------------Reading-------------");
        AnnotationReader<S> reader = AnnotationReader.read(dispatcher, annotationRegistry, instance);
        //System.out.println("-----------Accepting-----------");
        reader.accept(dispatcher, visitor);
        //System.out.println("-------Visited class `" + instanceClazz.getSimpleName() + "` --------");
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
