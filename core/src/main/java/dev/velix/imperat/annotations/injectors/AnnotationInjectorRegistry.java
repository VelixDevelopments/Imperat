package dev.velix.imperat.annotations.injectors;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.AnnotationLevel;
import dev.velix.imperat.annotations.AnnotationParser;
import dev.velix.imperat.annotations.AnnotationReader;
import dev.velix.imperat.annotations.AnnotationRegistry;
import dev.velix.imperat.annotations.element.CommandAnnotatedElement;
import dev.velix.imperat.annotations.injectors.context.InjectionContext;
import dev.velix.imperat.annotations.injectors.context.ProxyCommand;
import dev.velix.imperat.annotations.types.Command;
import dev.velix.imperat.annotations.types.Description;
import dev.velix.imperat.annotations.types.Permission;
import dev.velix.imperat.annotations.types.Async;
import dev.velix.imperat.annotations.types.Cooldown;
import dev.velix.imperat.annotations.types.Usage;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.CooldownHolder;
import dev.velix.imperat.command.DescriptionHolder;
import dev.velix.imperat.command.PermissionHolder;
import dev.velix.imperat.util.Registry;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class AnnotationInjectorRegistry<C>  extends Registry<InjectionContext, AnnotationDataInjector<?, C, ?>> {
	
	private AnnotationInjectorRegistry(Imperat<C> dispatcher) {
		registerBasicInjectors(dispatcher);
	}

	
	private void registerBasicInjectors(Imperat<C> dispatcher) {
		TypeWrap<dev.velix.imperat.command.Command<C>> commandTypeWrap = new TypeWrap<>() {};
		TypeWrap<CommandUsage<C>> commandUsageTypeWrap = new TypeWrap<>() {};
		
		registerInjector(Command.class, commandTypeWrap, AnnotationLevel.CLASS, new ProxyCommandInjector<>(dispatcher));
		registerInjector(Command.class, commandTypeWrap, AnnotationLevel.METHOD, new CommandMethodInjector<>(dispatcher));
		
		registerInjector(Usage.class, commandUsageTypeWrap, AnnotationLevel.METHOD, new UsageInjector<>(dispatcher));
		
		registerInjector(Cooldown.class, TypeWrap.of(CooldownHolder.class)
						, AnnotationLevel.METHOD, new CooldownInjector<>(dispatcher));
		
		registerInjector(Async.class, commandUsageTypeWrap, AnnotationLevel.METHOD, new AsyncInjector<>(dispatcher));
		//registering wildcards
		for(AnnotationLevel level : AnnotationLevel.values()) {
			if(level == AnnotationLevel.WILDCARD) continue;
			registerDescriptionInjector(dispatcher, level);
			registerPermissionInjector(dispatcher, level);
		}
		
	}
	private void registerDescriptionInjector(Imperat<C> dispatcher, AnnotationLevel level) {
		registerInjector(Description.class,
						TypeWrap.of(DescriptionHolder.class),
						level,
						new DescriptionInjector<>(dispatcher, level)
		);
	}
	
	private void registerPermissionInjector(Imperat<C> dispatcher, AnnotationLevel level) {
		registerInjector(
						Permission.class,
						TypeWrap.of(PermissionHolder.class),
						level, new PermissionInjector<>(dispatcher, level)
		);
	}
	
	public static <C> AnnotationInjectorRegistry<C> create(Imperat<C> dispatcher) {
		return new AnnotationInjectorRegistry<>(dispatcher);
	}
	
	public <O, A extends Annotation> void registerInjector(
					Class<A> annotationType,
					TypeWrap<O> typeToLoad,
					AnnotationLevel level,
					AnnotationDataInjector<O, C, A> injector
	) {
		this.setData(InjectionContext.of(annotationType, typeToLoad, level), injector);
	}
	
	@SuppressWarnings("unchecked")
	public <O, A extends Annotation, I extends AnnotationDataInjector<O, C, A>>
	Optional<I> getInjector(Class<A> annotationType, TypeWrap<O> typeToLoad, AnnotationLevel level) {
		return (Optional<I>) getData(InjectionContext.of(annotationType, typeToLoad, level));
	}
	
	public void forEachInjector(
					Predicate<AnnotationDataInjector<?, C, ?>> predicate,
					Consumer<AnnotationDataInjector<?, C, ?>> action
	) {
		for(var injector : getAll()) {
			if(predicate.test(injector)){
				action.accept(injector);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public <A extends Annotation, O> void injectForElement(
					ProxyCommand<C> proxyCommand,
					@NotNull AnnotationReader reader,
					@NotNull AnnotationParser<C> parser,
					@NotNull AnnotationRegistry annotationRegistry,
					@NotNull AnnotationInjectorRegistry<C> injectorRegistry,
					@NotNull CommandAnnotatedElement<?> element,
					@NotNull AnnotationLevel level
	) {
		
		A mainAnn = (A) annotationRegistry.getMainAnnotation(element);
		if(mainAnn == null) {
			return;
		}
		forEachInjector(
			(inj)-> inj.getContext().hasAnnotationType(mainAnn.annotationType())
							&& inj.getContext().isOnLevel(level)
			, (injector)-> {
				((AnnotationDataInjector<O, C, A>) injector).inject(proxyCommand, null, reader, parser,
								annotationRegistry, injectorRegistry, element, mainAnn);
			}
		);
		
	}
	
	public static void logError(Method method, Class<?> proxy, String msg) {
		throw new IllegalArgumentException(String.format(msg + " in method '%s' of class '%s'", method.getName(), proxy.getName()));
	}
}
