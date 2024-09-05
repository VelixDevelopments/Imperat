package dev.velix.imperat.annotations.injectors;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.AnnotationLevel;
import dev.velix.imperat.annotations.AnnotationParser;
import dev.velix.imperat.annotations.AnnotationReader;
import dev.velix.imperat.annotations.AnnotationRegistry;
import dev.velix.imperat.annotations.element.CommandAnnotatedElement;
import dev.velix.imperat.annotations.injectors.context.InjectionContext;
import dev.velix.imperat.annotations.injectors.context.ProxyCommand;
import dev.velix.imperat.annotations.types.methods.Async;
import dev.velix.imperat.command.CommandCoordinator;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
final class AsyncInjector<C> extends AnnotationDataInjector<CommandUsage<C>, C, Async> {
	
	public AsyncInjector(Imperat<C> dispatcher) {
		super(dispatcher, InjectionContext.of(Async.class, TypeWrap.of(CommandUsage.class), AnnotationLevel.METHOD));
	}
	
	@Override
	public @NotNull <T> CommandUsage<C> inject(
					ProxyCommand<C> proxyCommand,
					@Nullable CommandUsage<C> toLoad,
					AnnotationReader reader,
					AnnotationParser<C> parser,
					AnnotationRegistry annotationRegistry,
					AnnotationInjectorRegistry<C> injectorRegistry,
					@NotNull CommandAnnotatedElement<?> element,
					@NotNull Async annotation
	) {
		//TODO remove this from other injectors using DRY.
		if(toLoad == null) {
			throw new IllegalArgumentException("toLoad ,in @Async injection, is null.");
		}
		toLoad.setCoordinator(CommandCoordinator.async());
		return toLoad;
	}
}
