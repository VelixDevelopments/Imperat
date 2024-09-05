package dev.velix.imperat.annotations.injectors;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.*;
import dev.velix.imperat.annotations.element.CommandAnnotatedElement;
import dev.velix.imperat.annotations.injectors.context.InjectionContext;
import dev.velix.imperat.annotations.injectors.context.ProxyCommand;
import dev.velix.imperat.annotations.types.SubCommand;
import dev.velix.imperat.annotations.types.Inherit;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.util.CommandDebugger;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@SuppressWarnings({"unchecked", "unused"})
final class InheritanceInjector<C> extends AnnotationDataInjector<dev.velix.imperat.command.Command<C>, C, Inherit> {
	
	public InheritanceInjector(Imperat<C> dispatcher) {
		super(dispatcher, InjectionContext.of(Inherit.class, TypeWrap.of(Command.class), AnnotationLevel.CLASS));
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
					@NotNull Inherit annotation
	) {
		if(toLoad == null) throw new IllegalArgumentException("toLoad in injection of `@Inherit` is null");
		Class<?>[] subClasses = annotation.value();
		
		//Injecting subcommands recursively
		for(Class<?> subClass : subClasses) {
			if(!subClass.isAnnotationPresent(SubCommand.class)) {
				continue;
			}
			SubCommand subAnn = subClass.getAnnotation(SubCommand.class);
			var subCmd = parser.parseCommandClass(
							subAnnotationToCmdAnnotation(subAnn),
							reader,
							element,
							(T)getSubCommandInstance(subClass),
							(Class<T>)subClass
			);
			toLoad.addSubCommand(subCmd, subAnn.attachDirectly());
		}
		return toLoad;
	}
	
	private <T> T getSubCommandInstance(Class<T> subClass) {
		try {
			Constructor<?> constructor = subClass.getConstructor();
			return (T) constructor.newInstance();
		} catch (NoSuchMethodException | InvocationTargetException
		         | InstantiationException | IllegalAccessException e) {
			CommandDebugger.error(
							subClass, "constructor",
							e, "no empty public constructor"
			);
			throw new RuntimeException(e);
		}
		
	}
	
	private dev.velix.imperat.annotations.types.Command subAnnotationToCmdAnnotation(SubCommand subAnn) {
		return AnnotationFactory.create(dev.velix.imperat.annotations.types.Command.class,
						"value", subAnn.value(), "ignoreAutoCompletionChecks", subAnn.ignoreAutoCompletionChecks());
	}
	
}