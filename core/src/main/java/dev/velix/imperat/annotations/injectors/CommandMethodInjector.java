package dev.velix.imperat.annotations.injectors;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.*;
import dev.velix.imperat.annotations.element.CommandAnnotatedElement;
import dev.velix.imperat.annotations.injectors.context.InjectionContext;
import dev.velix.imperat.annotations.injectors.context.ProxyCommand;
import dev.velix.imperat.annotations.types.Usage;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
final class CommandMethodInjector<S extends Source> extends AnnotationDataInjector<Command<S>, S, dev.velix.imperat.annotations.types.Command> {

    public CommandMethodInjector(
            Imperat<S> imperat
    ) {
        super(imperat, InjectionContext.of(
                dev.velix.imperat.annotations.types.Command.class,
                TypeWrap.of(Command.class), AnnotationLevel.METHOD)
        );
    }

    @Override
    public <T> @NotNull Command<S> inject(
            ProxyCommand<S> proxyCommand,
            @Nullable Command<S> toLoad,
            AnnotationReader reader,
            AnnotationParser<S> parser,
            AnnotationRegistry annotationRegistry,
            AnnotationInjectorRegistry<S> injectorRegistry,
            @NotNull CommandAnnotatedElement<?> element,
            dev.velix.imperat.annotations.types.@NotNull Command annotation
    ) {

        final String[] values = annotation.value();
        List<String> aliases = new ArrayList<>(Arrays.asList(values)
                .subList(1, values.length));

        Method method = (Method) element.getElement();

        Command.Builder<S> builder = Command.<S>create(values[0])
                .ignoreACPermissions(annotation.ignoreAutoCompletionPermission())
                .aliases(aliases);

        if (method.getParameters().length == 1) {
            //default usage for that command.
            builder.defaultExecution(
                    new MethodCommandExecutor<>(proxyCommand, this.dispatcher, method, Collections.emptyList())
            );
        } else {
            var usageInjector = injectorRegistry.getInjector(Usage.class, new TypeWrap<CommandUsage<S>>() {
                    }, AnnotationLevel.METHOD)
                    .orElseThrow(() -> new IllegalStateException("Could not find injector for CommandUsage in CommandMethodInjector"));
            builder.usage(usageInjector.inject(proxyCommand, null, reader, parser, annotationRegistry, injectorRegistry, element, element.getAnnotation(Usage.class)));
        }

        return builder.build();
    }

}
