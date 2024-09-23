package dev.velix.imperat;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.NotNull;

/**
 * A class that manages parsing {@link Command}
 * into brigadier {@link BrigadierNode}
 *
 * @param <S> the command-source type
 */
public sealed interface BrigadierManager<S extends Source> permits BaseBrigadierManager {
    
    /**
     * Converts the original command source from brigadier
     * into the platform's command-source
     *
     * @param commandSource the command source
     * @return the platform's command sender/source
     */
    S wrapCommandSource(Object commandSource);
    
    /**
     * Registers the argument type to its class type
     *
     * @param type                 the type to register to the value-type obj
     * @param argumentTypeResolver the value type resolver
     * @param <T>                  the type parameter for the type.
     */
    <T> void registerArgumentResolver(Class<T> type, ArgumentTypeResolver argumentTypeResolver);
    
    /**
     * Registers the argument type resolver
     *
     * @param argumentTypeResolver the value type resolver
     */
    void registerArgumentResolver(ArgumentTypeResolver argumentTypeResolver);
    
    
    /**
     * Fetches the argument type from the parameter
     *
     * @param parameter the parameter
     * @return the {@link ArgumentType} for the {@link CommandParameter}
     */
    @NotNull
    ArgumentType<?> getArgumentType(CommandParameter<S> parameter);
    
    /**
     * Parses the registered {@link Command} to brigadier node
     *
     * @return the parsed node
     */
    <CN extends CommandNode<?>> CN parseCommandIntoNode(Command<S> command);
    
}
