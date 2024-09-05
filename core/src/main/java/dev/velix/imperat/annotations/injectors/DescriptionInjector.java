package dev.velix.imperat.annotations.injectors;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.AnnotationLevel;
import dev.velix.imperat.annotations.AnnotationParser;
import dev.velix.imperat.annotations.AnnotationReader;
import dev.velix.imperat.annotations.AnnotationRegistry;
import dev.velix.imperat.annotations.element.CommandAnnotatedElement;
import dev.velix.imperat.annotations.injectors.context.InjectionContext;
import dev.velix.imperat.annotations.injectors.context.ProxyCommand;
import dev.velix.imperat.annotations.types.Description;
import dev.velix.imperat.command.DescriptionHolder;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
final class DescriptionInjector<S extends Source> extends AnnotationDataInjector<DescriptionHolder, S, Description> {
    
    public DescriptionInjector(Imperat<S> dispatcher, AnnotationLevel level) {
        super(dispatcher, InjectionContext.of(Description.class, TypeWrap.of(DescriptionHolder.class), level));
    }
    
    @Override
    public @NotNull <T> DescriptionHolder inject(
            ProxyCommand<S> proxyCommand,
            @Nullable DescriptionHolder toLoad,
            AnnotationReader reader,
            AnnotationParser<S> parser,
            AnnotationRegistry annotationRegistry,
            AnnotationInjectorRegistry<S> injectorRegistry,
            @NotNull CommandAnnotatedElement<?> element,
            @NotNull Description annotation
    ) {
        if (toLoad == null) {
            throw new IllegalArgumentException("toLoad ,in @Description injection, is null.");
        }
        toLoad.setDescription(annotation.value());
        return toLoad;
    }
    
}
