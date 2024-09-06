package dev.velix.imperat.annotations.injectors;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.AnnotationLevel;
import dev.velix.imperat.annotations.AnnotationParser;
import dev.velix.imperat.annotations.AnnotationReader;
import dev.velix.imperat.annotations.AnnotationRegistry;
import dev.velix.imperat.annotations.element.CommandAnnotatedElement;
import dev.velix.imperat.annotations.injectors.context.InjectionContext;
import dev.velix.imperat.annotations.injectors.context.ProxyCommand;
import dev.velix.imperat.annotations.types.*;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.CooldownHolder;
import dev.velix.imperat.command.DescriptionHolder;
import dev.velix.imperat.command.PermissionHolder;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.util.Registry;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class AnnotationInjectorRegistry<S extends Source> extends Registry<InjectionContext, AnnotationDataInjector<?, S, ?>> {

    private AnnotationInjectorRegistry(Imperat<S> dispatcher) {
        registerBasicInjectors(dispatcher);
    }

    public static <S extends Source> AnnotationInjectorRegistry<S> create(Imperat<S> dispatcher) {
        return new AnnotationInjectorRegistry<>(dispatcher);
    }

    public static void logError(Method method, Class<?> proxy, String msg) {
        throw new IllegalArgumentException(String.format(msg + " in method '%s' of class '%s'", method.getName(), proxy.getName()));
    }

    private void registerBasicInjectors(Imperat<S> dispatcher) {
        TypeWrap<dev.velix.imperat.command.Command<S>> commandTypeWrap = new TypeWrap<>() {
        };
        TypeWrap<CommandUsage<S>> commandUsageTypeWrap = new TypeWrap<>() {
        };

        registerInjector(Command.class, commandTypeWrap, AnnotationLevel.CLASS, new ProxyCommandInjector<>(dispatcher));
        registerInjector(Inherit.class, commandTypeWrap, AnnotationLevel.CLASS, new InheritanceInjector<>(dispatcher));

        registerInjector(Command.class, commandTypeWrap, AnnotationLevel.METHOD, new CommandMethodInjector<>(dispatcher));

        registerInjector(Usage.class, commandUsageTypeWrap, AnnotationLevel.METHOD, new UsageInjector<>(dispatcher));
        registerInjector(SubCommand.class, commandTypeWrap, AnnotationLevel.METHOD, new SubCommandInjector<>(dispatcher));

        registerInjector(Named.class, TypeWrap.of(CommandParameter.class), AnnotationLevel.PARAMETER, new CommandParameterInjector<>(dispatcher));
        registerInjector(Cooldown.class, TypeWrap.of(CooldownHolder.class)
                , AnnotationLevel.METHOD, new CooldownInjector<>(dispatcher));

        registerInjector(Async.class, commandUsageTypeWrap, AnnotationLevel.METHOD, new AsyncInjector<>(dispatcher));

        //registering wildcards
        for (AnnotationLevel level : AnnotationLevel.values()) {
            if (level == AnnotationLevel.WILDCARD) continue;
            registerDescriptionInjector(dispatcher, level);
            registerPermissionInjector(dispatcher, level);
        }

    }

    private void registerDescriptionInjector(Imperat<S> dispatcher, AnnotationLevel level) {
        registerInjector(Description.class,
                TypeWrap.of(DescriptionHolder.class),
                level,
                new DescriptionInjector<>(dispatcher, level)
        );
    }

    private void registerPermissionInjector(Imperat<S> dispatcher, AnnotationLevel level) {
        registerInjector(
                Permission.class,
                TypeWrap.of(PermissionHolder.class),
                level, new PermissionInjector<>(dispatcher, level)
        );
    }

    public <O, A extends Annotation> void registerInjector(
            Class<A> annotationType,
            TypeWrap<O> typeToLoad,
            AnnotationLevel level,
            AnnotationDataInjector<O, S, A> injector
    ) {
        this.setData(InjectionContext.of(annotationType, typeToLoad, level), injector);
    }

    @SuppressWarnings("unchecked")
    public <O, A extends Annotation, I extends AnnotationDataInjector<O, S, A>>
    Optional<I> getInjector(Class<A> annotationType, TypeWrap<O> typeToLoad, AnnotationLevel level) {
        for (var injector : getAll()) {
            var ctx = injector.getContext();
            //debugInj(injector);
            //System.out.println(String.format("VALUES= {actual-annotation-target=%s, actual-toLoad=%s, actual-level=%s}", annotationType.getSimpleName(), typeToLoad.getType().getTypeName(), level.name()));
            if (ctx.isOnLevel(level) && ctx.hasAnnotationType(annotationType) && ctx.hasTargetType(typeToLoad)) {
                return Optional.of((I) injector);
            }
        }
        return Optional.empty();
    }

    private void debugInj(AnnotationDataInjector<?, S, ?> injector) {
        System.out.println(injector.toString());
    }

    public void forEachInjector(
            Predicate<AnnotationDataInjector<?, S, ?>> predicate,
            Consumer<AnnotationDataInjector<?, S, ?>> action
    ) {
        for (var injector : getAll()) {
            if (predicate.test(injector)) {
                action.accept(injector);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <A extends Annotation, O> void injectForElement(
            ProxyCommand<S> proxyCommand,
            @NotNull AnnotationReader reader,
            @NotNull AnnotationParser<S> parser,
            @NotNull AnnotationRegistry annotationRegistry,
            @NotNull AnnotationInjectorRegistry<S> injectorRegistry,
            @NotNull CommandAnnotatedElement<?> element,
            @NotNull AnnotationLevel level,
            TypeWrap<O> targetToLoad,
            @NotNull O toLoad
    ) {

        A mainAnn = (A) annotationRegistry.getMainAnnotation(element);
        if (mainAnn == null) {
            return;
        }

        for (var inj : injectorRegistry.getAll()) {
            if (!inj.getContext().hasAnnotationType(mainAnn.annotationType())
                    && inj.getContext().isOnLevel(level)
                    && inj.getContext().hasTargetType(targetToLoad)) {
                Annotation annotation = element.getAnnotation(inj.getContext().annClass());
                if (annotation instanceof Inherit) continue;
                if (annotation == null) continue;
                ((AnnotationDataInjector<O, S, A>) inj).inject(proxyCommand, toLoad, reader, parser,
                        annotationRegistry, injectorRegistry, element, (A) annotation);
            }
        }

    }
}
