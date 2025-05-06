package dev.velix.imperat.command.returns;

import dev.velix.imperat.context.Source;
import dev.velix.imperat.util.TypeWrap;

import java.lang.reflect.Type;

public abstract class BaseReturnResolver<S extends Source, T> implements ReturnResolver<S, T> {

    private final Type type;

    public BaseReturnResolver(Type type) {
        this.type = type;
    }

    public BaseReturnResolver(TypeWrap<T> type) {
        this.type = type.getType();
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "BaseReturnResolver{" +
            "type=" + type +
            '}';
    }

}
