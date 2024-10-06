package dev.velix.imperat;

import dev.velix.imperat.context.Source;

import java.lang.reflect.Type;

public sealed interface SourceWrapper<S extends Source> permits Imperat {
    /**
     * Wraps the sender into a built-in command-sender valueType
     *
     * @param sender the sender's actual value
     * @return the wrapped command-sender valueType
     */
    S wrapSender(Object sender);

    /**
     * Checks whether the valueType can be a command sender
     *
     * @param type the valueType
     * @return whether the valueType can be a command sender
     */
    boolean canBeSender(Type type);
}
