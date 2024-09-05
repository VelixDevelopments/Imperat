package dev.velix.imperat.annotations.injectors;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.AnnotationLevel;
import dev.velix.imperat.annotations.AnnotationParser;
import dev.velix.imperat.annotations.AnnotationReader;
import dev.velix.imperat.annotations.AnnotationRegistry;
import dev.velix.imperat.annotations.element.CommandAnnotatedElement;
import dev.velix.imperat.annotations.injectors.context.InjectionContext;
import dev.velix.imperat.annotations.injectors.context.ProxyCommand;
import dev.velix.imperat.annotations.types.methods.Cooldown;
import dev.velix.imperat.command.CooldownHolder;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
final class CooldownInjector<C> extends
				AnnotationDataInjector<CooldownHolder, C, Cooldown> {
	
	public CooldownInjector(Imperat<C> dispatcher) {
		super(dispatcher, InjectionContext.of(Cooldown.class, TypeWrap.of(CooldownHolder.class), AnnotationLevel.METHOD));
	}
	
	@Override
	public @NotNull <T> CooldownHolder inject(
					ProxyCommand<C> proxyCommand,
					@Nullable CooldownHolder toLoad,
					AnnotationReader reader,
					AnnotationParser<C> parser,
					AnnotationRegistry annotationRegistry,
					AnnotationInjectorRegistry<C> injectorRegistry,
					@NotNull CommandAnnotatedElement<?> element,
					@NotNull Cooldown annotation
	) {
		if(toLoad == null) {
			throw new IllegalArgumentException("toLoad ,in @Cooldown injection, is null.");
		}
		toLoad.setCooldown(annotation.value(), annotation.unit());
		return toLoad;
	}
}
