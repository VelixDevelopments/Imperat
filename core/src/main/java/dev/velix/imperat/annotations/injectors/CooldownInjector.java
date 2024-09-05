package dev.velix.imperat.annotations.injectors;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.AnnotationLevel;
import dev.velix.imperat.annotations.AnnotationParser;
import dev.velix.imperat.annotations.AnnotationReader;
import dev.velix.imperat.annotations.AnnotationRegistry;
import dev.velix.imperat.annotations.element.CommandAnnotatedElement;
import dev.velix.imperat.annotations.injectors.context.InjectionContext;
import dev.velix.imperat.annotations.injectors.context.ProxyCommand;
import dev.velix.imperat.annotations.types.Cooldown;
import dev.velix.imperat.command.CooldownHolder;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
final class CooldownInjector<S extends Source> extends
        AnnotationDataInjector<CooldownHolder, S, Cooldown> {

    public CooldownInjector(Imperat<S> dispatcher) {
        super(dispatcher, InjectionContext.of(Cooldown.class, TypeWrap.of(CooldownHolder.class), AnnotationLevel.METHOD));
    }

    @Override
    public @NotNull <T> CooldownHolder inject(
            ProxyCommand<S> proxyCommand,
            @Nullable CooldownHolder toLoad,
            AnnotationReader reader,
            AnnotationParser<S> parser,
            AnnotationRegistry annotationRegistry,
            AnnotationInjectorRegistry<S> injectorRegistry,
            @NotNull CommandAnnotatedElement<?> element,
            @NotNull Cooldown annotation
    ) {
        if (toLoad == null) {
            throw new IllegalArgumentException("toLoad ,in @Cooldown injection, is null.");
        }
        toLoad.setCooldown(annotation.value(), annotation.unit());
        return toLoad;
    }
}
