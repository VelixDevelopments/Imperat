package dev.velix.imperat.annotations.injectors;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.AnnotationLevel;
import dev.velix.imperat.annotations.AnnotationParser;
import dev.velix.imperat.annotations.AnnotationReader;
import dev.velix.imperat.annotations.AnnotationRegistry;
import dev.velix.imperat.annotations.element.CommandAnnotatedElement;
import dev.velix.imperat.annotations.injectors.context.InjectionContext;
import dev.velix.imperat.annotations.injectors.context.ProxyCommand;
import dev.velix.imperat.annotations.types.Permission;
import dev.velix.imperat.command.PermissionHolder;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
final class PermissionInjector<S extends Source> extends AnnotationDataInjector<PermissionHolder, S, Permission> {

    public PermissionInjector(Imperat<S> dispatcher, AnnotationLevel level) {
        super(dispatcher, InjectionContext.of(Permission.class, TypeWrap.of(PermissionHolder.class), level));
    }

    @Override
    public @NotNull <T> PermissionHolder inject(
            ProxyCommand<S> proxyCommand,
            @Nullable PermissionHolder toLoad,
            AnnotationReader reader,
            AnnotationParser<S> parser,
            AnnotationRegistry annotationRegistry,
            AnnotationInjectorRegistry<S> injectorRegistry,
            @NotNull CommandAnnotatedElement<?> element,
            @NotNull Permission annotation
    ) {
        if (toLoad == null) {
            throw new IllegalArgumentException("toLoad ,in @Permission injection, is null.");
        }
        toLoad.setPermission(annotation.value());
        return toLoad;
    }

}
