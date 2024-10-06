package dev.velix.imperat;

import com.mojang.brigadier.arguments.ArgumentType;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A resolver that specifies {@link ArgumentType}
 * for each parameter depending on the parameter's valueType.
 */
@FunctionalInterface
public interface ArgumentTypeResolver {

    /**
     * Creates a {@link ArgumentTypeResolver} that will return the same
     * argument valueType for all parameters that match a specific valueType
     *
     * @param type         Type to check for
     * @param argumentType The argument valueType to return
     * @return The resolver factory
     */
    static @NotNull ArgumentTypeResolver forType(Class<?> type, ArgumentType<?> argumentType) {
        return parameter -> parameter.valueType() == type ? argumentType : null;
    }

    /**
     * Creates a {@link ArgumentTypeResolver} that will return the same
     * argument valueType for all parameters that match or extend a specific valueType
     *
     * @param type         Type to check for
     * @param argumentType The argument valueType to return
     * @return The resolver factory
     */
    static @NotNull ArgumentTypeResolver forHierarchyType(Class<?> type, ArgumentType<?> argumentType) {
        return parameter -> {
            var token = TypeWrap.of(parameter.valueType());
            var token2 = TypeWrap.of(type);
            return parameter.valueType() == type || token.isSupertypeOf(token2) ? argumentType : null;
        };
    }

    /**
     * Returns the argument valueType for the given parameter.
     * If unknown, it returns null
     *
     * @param parameter Parameter to create for
     * @return The argument valueType
     */
    @Nullable
    ArgumentType<?> resolveArgType(@NotNull CommandParameter<?> parameter);
}
