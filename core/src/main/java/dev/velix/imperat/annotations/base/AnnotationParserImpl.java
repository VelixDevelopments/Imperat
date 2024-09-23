package dev.velix.imperat.annotations.base;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.base.element.CommandClassVisitor;
import dev.velix.imperat.annotations.base.element.MethodElement;
import dev.velix.imperat.annotations.base.element.selector.ElementSelector;
import dev.velix.imperat.annotations.base.element.selector.MethodRules;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.Annotation;

@ApiStatus.Internal
final class AnnotationParserImpl<S extends Source> extends AnnotationParser<S> {
    
    private final AnnotationRegistry annotationRegistry;
    private final ElementSelector<MethodElement> methodSelector;
    private final CommandClassVisitor<S> visitor;
    
    AnnotationParserImpl(Imperat<S> dispatcher, AnnotationRegistry annotationRegistry, CommandClassVisitor<S> visitor) {
        super(dispatcher);
        this.annotationRegistry = annotationRegistry;
        
        this.methodSelector = ElementSelector.create();
        methodSelector.addRule(MethodRules.IS_PUBLIC.and(MethodRules.RETURNS_VOID));
        
        this.visitor = visitor;
    }
    
    
    @Override
    public <T> void parseCommandClass(T instance) {
        AnnotationReader<S> reader = AnnotationReader.read(dispatcher, methodSelector, annotationRegistry, instance);
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
