package dev.velix.imperat.annotations.injectors;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.AnnotationLevel;
import dev.velix.imperat.annotations.AnnotationParser;
import dev.velix.imperat.annotations.AnnotationReader;
import dev.velix.imperat.annotations.AnnotationRegistry;
import dev.velix.imperat.annotations.element.CommandAnnotatedElement;
import dev.velix.imperat.annotations.injectors.context.InjectionContext;
import dev.velix.imperat.annotations.injectors.context.ProxyCommand;
import dev.velix.imperat.annotations.types.Inherit;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
final class ProxyCommandInjector<C> extends AnnotationDataInjector
				<Command<C>, C, dev.velix.imperat.annotations.types.Command> {
	
	public ProxyCommandInjector(Imperat<C> dispatcher) {
		super(dispatcher, InjectionContext.of(dev.velix.imperat.annotations.types.Command.class, TypeWrap.of(Command.class), AnnotationLevel.CLASS));
	}
	
	@Override
	public @NotNull <T> Command<C> inject(
					@Nullable ProxyCommand<C> proxyCommand,
					@Nullable Command<C> toLoad,
					AnnotationReader reader,
					AnnotationParser<C> parser,
					AnnotationRegistry annotationRegistry,
					AnnotationInjectorRegistry<C> injectorRegistry,
					@NotNull CommandAnnotatedElement<?> element,
					dev.velix.imperat.annotations.types.@NotNull Command annotation
	) {
		final String[] values = annotation.value();
		List<String> aliases = new ArrayList<>(Arrays.asList(values)
						.subList(1, values.length));
		
		Command<C> command = Command.<C>create(values[0])
						.ignoreACPermissions(annotation.ignoreAutoCompletionPermission())
						.aliases(aliases)
						.build();
		
		if(element.isAnnotationPresent(Inherit.class)) {
			var inheritanceInjector = injectorRegistry.getInjector(Inherit.class, new TypeWrap<Command<C>>(){}, AnnotationLevel.CLASS).orElseThrow();
			inheritanceInjector.inject(proxyCommand, command, reader, parser, annotationRegistry, injectorRegistry, element, element.getAnnotation(Inherit.class));
		}
		return command;
	}
}
