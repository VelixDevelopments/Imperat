package dev.velix.imperat.annotations.injectors;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.*;
import dev.velix.imperat.annotations.element.CommandAnnotatedElement;
import dev.velix.imperat.annotations.injectors.context.InjectionContext;
import dev.velix.imperat.annotations.injectors.context.ProxyCommand;
import dev.velix.imperat.annotations.types.methods.Usage;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
final class CommandMethodInjector<C> extends AnnotationDataInjector<Command<C>, C, dev.velix.imperat.annotations.types.Command> {
	
	public CommandMethodInjector(
					Imperat<C> imperat
	) {
		super(imperat, InjectionContext.of(
						dev.velix.imperat.annotations.types.Command.class,
						TypeWrap.of(Command.class), AnnotationLevel.METHOD)
		);
	}
	
	@Override
	public <T> @NotNull Command<C> inject(
					ProxyCommand<C> proxyCommand,
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
		
		Method method = (Method) element.getElement();
		
		Command.Builder<C> builder = Command.<C>create(values[0])
						.ignoreACPermissions(annotation.ignoreAutoCompletionPermission())
						.aliases(aliases);
		
		if(method.getParameters().length == 1) {
			//default usage for that command.
			builder.defaultExecution(
							new MethodCommandExecutor<>(proxyCommand, this.dispatcher, method, Collections.emptyList())
			);
		}else {
			var usageInjector = injectorRegistry.getInjector(Usage.class, new TypeWrap<CommandUsage<C>>(){}, AnnotationLevel.METHOD)
							.orElseThrow(()-> new IllegalStateException("Could not find injector for CommandUsage in CommandMethodInjector"));
			builder.usage(usageInjector.inject(proxyCommand, null, reader, parser, annotationRegistry, injectorRegistry, element, element.getAnnotation(Usage.class)));
		}
		
		return builder.build();
	}
	
}
