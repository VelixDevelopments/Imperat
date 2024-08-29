package dev.velix.imperat;

import com.mojang.brigadier.arguments.ArgumentType;
import dev.velix.imperat.command.parameters.CommandParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A resolver that specifies {@link ArgumentType}
 * for each parameter depending on the parameter's type.
 */
@FunctionalInterface
public interface ArgumentTypeResolver {

    /**
     * Returns the argument type for the given parameter.
     * If unknown, it returns null
     *
     * @param parameter Parameter to create for
     * @return The argument type
     */
    @Nullable
    ArgumentType<?> resolveArgType(@NotNull CommandParameter parameter);

    /**
     * Creates a {@link ArgumentTypeResolver} that will return the same
     * argument type for all parameters that match a specific type
     *
     * @param type         Type to check for
     * @param argumentType The argument type to return
     * @return The resolver factory
     */
    static @NotNull ArgumentTypeResolver forType(Class<?> type, ArgumentType<?> argumentType) {
        return parameter -> parameter.getType() == type ? argumentType : null;
    }

    /**
     * Creates a {@link ArgumentTypeResolver} that will return the same
     * argument type for all parameters that match or extend a specific type
     *
     * @param type         Type to check for
     * @param argumentType The argument type to return
     * @return The resolver factory
     */
    static @NotNull ArgumentTypeResolver forHierarchyType(Class<?> type, ArgumentType<?> argumentType) {
        return parameter -> parameter.getType() == type || parameter.getType().isAssignableFrom(type) ? argumentType : null;
    }
}
