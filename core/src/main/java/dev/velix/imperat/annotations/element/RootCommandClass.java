package dev.velix.imperat.annotations.element;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class RootCommandClass<S extends Source> {
    
    private final Class<?> proxyClass;
    private final Object proxyInstance;
    private @Nullable Command<S> commandLoaded;
    
    public RootCommandClass(Class<?> proxyClass, Object proxyInstance) {
        this.proxyClass = proxyClass;
        this.proxyInstance = proxyInstance;
    }
    
    public void setRootCommand(Command<S> commandLoaded) {
        this.commandLoaded = commandLoaded;
    }
    
    public boolean isCommand() {
        return commandLoaded != null;
    }
    
    public Class<?> proxyClass() {
        return proxyClass;
    }
    
    public Object proxyInstance() {
        return proxyInstance;
    }
    
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (RootCommandClass<?>) obj;
        return Objects.equals(this.proxyClass, that.proxyClass) &&
                Objects.equals(this.proxyInstance, that.proxyInstance);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(proxyClass, proxyInstance);
    }
    
    
}
