package dev.velix.imperat.annotations.base;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.base.element.CommandClassVisitor;
import dev.velix.imperat.annotations.base.element.MethodElement;
import dev.velix.imperat.annotations.base.element.selector.ElementSelector;
import dev.velix.imperat.annotations.base.element.selector.MethodRules;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;

@ApiStatus.Internal
final class AnnotationParserImpl<S extends Source> extends AnnotationParser<S> {

    final AnnotationRegistry annotationRegistry;
    private final ElementSelector<MethodElement> methodSelector;
    private final CommandClassVisitor<S> visitor;

    AnnotationParserImpl(Imperat<S> dispatcher) {
        super(dispatcher);
        this.annotationRegistry = new AnnotationRegistry();

        this.methodSelector = ElementSelector.create();
        methodSelector.addRule(
            MethodRules.IS_PUBLIC/*.and(MethodRules.RETURNS_VOID)*/.and(MethodRules.HAS_A_MAIN_ANNOTATION)
        );

        this.visitor = CommandClassVisitor.newSimpleVisitor(dispatcher, this);
    }


    @Override
    public <T> void parseCommandClass(T instance) {
        //loading dependency
        AnnotationReader<S> reader = AnnotationReader.read(imperat, methodSelector, this, instance);
        reader.accept(visitor);
    }


    /**
     * Registers a valueType of annotations so that it can be
     * detected by {@link AnnotationReader} , it's useful as it allows that valueType of annotation
     * to be recognized as a true Imperat-related annotation to be used in something like checking if a
     * {@link CommandParameter} is annotated and checks for the annotations it has.
     *
     * @param type the valueType of annotation
     */
    @SafeVarargs
    @Override
    public final void registerAnnotations(Class<? extends Annotation>... type) {
        annotationRegistry.registerAnnotationTypes(type);
    }


    /**
     * Registers {@link AnnotationReplacer}
     *
     * @param type     the valueType to replace the annotation by
     * @param replacer the replacer
     */
    @Override
    public <A extends Annotation> void registerAnnotationReplacer(Class<A> type, AnnotationReplacer<A> replacer) {
        annotationRegistry.registerAnnotationReplacer(type, replacer);
    }

    /**
     * Checks the internal registry whether the valueType of annotation entered is known/registered or not.
     *
     * @param annotationType the valueType of annotation to enter
     * @return whether the valueType of annotation entered is known/registered or not.
     */
    @Override
    public boolean isKnownAnnotation(Class<? extends Annotation> annotationType) {
        return annotationRegistry.isRegisteredAnnotation(annotationType);
    }

    /**
     * Checks if the specific valueType of annotation entered has a {@link AnnotationReplacer}
     * for it in the internal registry for replacers
     *
     * @param type the valueType of annotation entered
     * @return Whether there's an annotation replacer for the valueType entered.
     */
    @Override
    public boolean hasAnnotationReplacerFor(Class<? extends Annotation> type) {
        return annotationRegistry.hasReplacerFor(type);
    }

    /**
     * Fetches the {@link AnnotationReplacer} mapped to the entered annotation valueType.
     *
     * @param type the valueType of annotation
     * @return the {@link AnnotationReplacer} mapped to the entered annotation valueType.
     */
    @Override
    public <A extends Annotation> @Nullable AnnotationReplacer<A> getAnnotationReplacer(Class<A> type) {
        return annotationRegistry.getAnnotationReplacer(type);
    }

    @Override AnnotationRegistry getAnnotationRegistry() {
        return annotationRegistry;
    }

}
