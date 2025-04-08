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
import dev.velix.imperat.util.ImperatDebugger;
import org.jetbrains.annotations.*;

import java.lang.reflect.Method;
import java.util.List;

@ApiStatus.Internal
final class AnnotationReaderImpl<S extends Source> implements AnnotationReader<S> {

    //private final Comparator<Method> METHOD_COMPARATOR;

    private final Imperat<S> imperat;
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
        this.rootCommandClass = new RootCommandClass<>(instance.getClass(), instance);
        this.methodSelector = methodSelector;
        //METHOD_COMPARATOR = Comparator.comparingInt(m -> AnnotationHelper.loadMethodPriority(m, imperat.config()));
        this.classElement = read(imperat);
    }

    private ClassElement read(Imperat<S> imperat) {
        return readClass(imperat, parser, null, rootCommandClass.proxyClass());
    }

    private ClassElement readClass(
        Imperat<S> imperat,
        AnnotationParser<S> parser,
        @Nullable ClassElement parent,
        @NotNull Class<?> clazz
    ) {
        ClassElement root = new ClassElement(parser, parent, clazz);
        //Adding methods with their parameters
        List<Method> methods;
        try {
            methods = SourceOrderHelper.getMethodsInSourceOrder(clazz);
        } catch (Exception e) {
            ImperatDebugger.error(AnnotationReaderImpl.class, "readClass", e);
            throw new RuntimeException(e);
        }
        //Arrays.sort(methods, METHOD_COMPARATOR);
        for (Method method : methods) {
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
        List<Class<?>> innerClasses;
        try {
            innerClasses = SourceOrderHelper.getInnerClassesInSourceOrder(clazz);

        } catch (Exception e) {
            ImperatDebugger.error(AnnotationReaderImpl.class, "readClass", e);
            throw new RuntimeException(e);
        }

        for (Class<?> child : innerClasses) {
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
