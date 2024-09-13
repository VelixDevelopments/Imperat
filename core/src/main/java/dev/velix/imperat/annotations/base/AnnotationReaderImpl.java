package dev.velix.imperat.annotations.base;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.Inherit;
import dev.velix.imperat.annotations.base.element.ClassElement;
import dev.velix.imperat.annotations.base.element.CommandClassVisitor;
import dev.velix.imperat.annotations.base.element.MethodElement;
import dev.velix.imperat.annotations.base.element.RootCommandClass;
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
    
    private final Class<?> clazz;
    private final AnnotationRegistry registry;
    
    private final RootCommandClass<S> rootCommandClass;
    private final ClassElement classElement;
    
    AnnotationReaderImpl(Imperat<S> imperat, AnnotationRegistry registry, Object instance) {
        this.registry = registry;
        this.clazz = instance.getClass();
        this.rootCommandClass = new RootCommandClass<>(clazz, instance);
        this.classElement = read(imperat);
    }
    
    private ClassElement read(Imperat<S> imperat) {
        return readClass(imperat, registry, null, clazz);
    }
    
    private ClassElement readClass(
            Imperat<S> imperat,
            AnnotationRegistry registry,
            @Nullable ClassElement parent,
            @NotNull Class<?> clazz
    ) {
        ClassElement root = new ClassElement(registry, parent, clazz);
        //Adding methods with their parameters
        Method[] methods = clazz.getDeclaredMethods();
        Arrays.sort(methods, METHOD_COMPARATOR);
        
        for (Method method : methods) {
            //System.out.println(clazz.getSimpleName() + ": adding method=" + method.getName());
            root.addChild(
                    new MethodElement(imperat, registry, root, method)
            );
        }
        
        //We add external subcommand classes from @Inherit as children
        if (root.isAnnotationPresent(Inherit.class)) {
            Inherit inherit = root.getAnnotation(Inherit.class);
            assert inherit != null;
            for (Class<?> subClass : inherit.value()) {
                root.addChild(
                        readClass(imperat, registry, root, subClass)
                );
            }
        }
        
        //Adding inner classes
        for (Class<?> child : clazz.getDeclaredClasses()) {
            root.addChild(
                    readClass(imperat, registry, root, child)
            );
        }
        
        
        return root;
    }
    
    
    @Override
    public RootCommandClass<S> getRootClass() {
        return rootCommandClass;
    }
    
    @Override
    public void accept(Imperat<S> dispatcher, CommandClassVisitor<S> visitor) {
        for (Command<S> loaded : classElement.accept(visitor)) {
            dispatcher.registerCommand(loaded);
        }
    }
    
}
