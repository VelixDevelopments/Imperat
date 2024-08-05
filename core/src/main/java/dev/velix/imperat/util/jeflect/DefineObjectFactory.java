package dev.velix.imperat.util.jeflect;

import com.github.romanqed.jfunc.Function1;

import java.util.Objects;
import java.util.concurrent.Callable;

public final class DefineObjectFactory<T> implements ObjectFactory<T> {
    private final DefineLoader loader;

    public DefineObjectFactory(DefineLoader loader) {
        this.loader = Objects.requireNonNull(loader);
    }

    public DefineLoader getLoader() {
        return loader;
    }

    @Override
    public T create(String name, Callable<byte[]> provider, Function1<Class<?>, ? extends T> creator) {
        var clazz = this.loader.load(name);
        try {
            if (clazz == null) {
                var bytes = provider.call();
                clazz = this.loader.define(name, bytes);
            }
            return creator.invoke(clazz);
        } catch (Error | RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T create(String name, Callable<byte[]> provider) {
        return create(name, provider, clazz -> (T) clazz.getDeclaredConstructor().newInstance());
    }
}
