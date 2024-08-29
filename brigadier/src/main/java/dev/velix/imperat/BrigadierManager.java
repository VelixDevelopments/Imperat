package dev.velix.imperat;

import com.mojang.brigadier.arguments.ArgumentType;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.parameters.CommandParameter;
import org.jetbrains.annotations.NotNull;

/**
 * A class that manages parsing {@link Command}
 * into brigadier {@link BrigadierNode}
 *
 * @param <C> the command-source type
 */
public interface BrigadierManager<C> {

    /**
     * Get the dispatcher
     *
     * @return {@link Imperat}
     */
    Imperat<C> getDispatcher();

    /**
     * Converts the original command source from brigadier
     * into the platform's command-source
     *
     * @param commandSource the command source
     * @return the platform's command sender/source
     */
    C wrapCommandSource(Object commandSource);

    /**
     * Registers the argument type to its class type
     *
     * @param type                 the type to register to the arg-type obj
     * @param argumentTypeResolver the arg type resolver
     * @param <T>                  the type parameter for the type.
     */
    <T> void registerArgumentResolver(Class<T> type, ArgumentTypeResolver argumentTypeResolver);

    /**
     * Registers the argument type resolver
     *
     * @param argumentTypeResolver the arg type resolver
     */
    void registerArgumentResolver(ArgumentTypeResolver argumentTypeResolver);


    /**
     * Fetches the argument type from the parameter
     *
     * @param parameter the parameter
     * @return the {@link ArgumentType} for the {@link CommandParameter}
     */
    @NotNull
    ArgumentType<?> getArgumentType(CommandParameter parameter);

    /**
     * Parses the registered {@link Command} to brigadier node
     *
     * @return the parsed node
     */
    BrigadierNode parseCommandIntoNode(Command<C> command);

}
