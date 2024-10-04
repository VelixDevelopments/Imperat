package dev.velix.imperat.annotations.base;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.Inherit;
import dev.velix.imperat.annotations.base.element.ClassElement;
import dev.velix.imperat.annotations.base.element.CommandClassVisitor;
import dev.velix.imperat.annotations.base.element.MethodElement;
import dev.velix.imperat.annotations.base.element.RootCommandClass;
import dev.velix.imperat.annotations.base.element.selector.ElementSelector;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

@ApiStatus.Internal
final class AnnotationReaderImpl<S extends Source> implements AnnotationReader<S> {
	
	private final static Comparator<Method> METHOD_COMPARATOR = Comparator.comparingInt(AnnotationHelper::loadMethodPriority);
	
	private final Imperat<S> imperat;
	private final Class<?> clazz;
	private final AnnotationParser<S> parser;
	private final ElementSelector<MethodElement> methodSelector;
	private final RootCommandClass<S> rootCommandClass;
	private final ClassElement classElement;
	
	AnnotationReaderImpl(
		Imperat<S> imperat,
		ElementSelector<MethodElement> methodSelector,
		AnnotationParser<S> parser,
		Object instance
	) {
		this.imperat = imperat;
		this.parser = parser;
		this.clazz = instance.getClass();
		this.rootCommandClass = new RootCommandClass<>(clazz, instance);
		this.methodSelector = methodSelector;
		this.classElement = read(imperat);
	}
	
	private ClassElement read(Imperat<S> imperat) {
		return readClass(imperat, parser, null, clazz);
	}
	
	private ClassElement readClass(
		Imperat<S> imperat,
		AnnotationParser<S> parser,
		@Nullable ClassElement parent,
		@NotNull Class<?> clazz
	) {
		ClassElement root = new ClassElement(parser, parent, clazz);
		//Adding methods with their parameters
		Method[] methods = clazz.getDeclaredMethods();
		Arrays.sort(methods, METHOD_COMPARATOR);
		
		for (Method method : methods) {
			//System.out.println(clazz.getSimpleName() + ": adding method=" + method.getName());
			MethodElement methodElement = new MethodElement(imperat, parser, root, method);
			if (methodSelector.canBeSelected(imperat, parser, methodElement, false)) {
				root.addChild(methodElement);
			}
		}
		
		//We add external subcommand classes from @Inherit as children
		if (root.isAnnotationPresent(Inherit.class)) {
			Inherit inherit = root.getAnnotation(Inherit.class);
			assert inherit != null;
			for (Class<?> subClass : inherit.value()) {
				root.addChild(
					readClass(imperat, parser, root, subClass)
				);
			}
		}
		
		//Adding inner classes
		for (Class<?> child : clazz.getDeclaredClasses()) {
			root.addChild(
				readClass(imperat, parser, root, child)
			);
		}
		
		
		return root;
	}
	
	
	@Override
	public RootCommandClass<S> getRootClass() {
		return rootCommandClass;
	}
	
	@Override
	public void accept(CommandClassVisitor<S> visitor) {
		for (Command<S> loaded : classElement.accept(visitor)) {
			imperat.registerCommand(loaded);
		}
	}
	
}
