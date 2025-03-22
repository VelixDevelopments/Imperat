package dev.velix.imperat;


import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.tree.CommandDispatch;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Represents the class that handles all
 * commands' registrations and executions
 * It also caches the settings that the user can
 * change or modify in the api.
 *
 * @param <S> the command sender valueType
 */
@ApiStatus.AvailableSince("1.0.0")
public non-sealed interface Imperat<S extends Source> extends AnnotationInjector<S>, CommandRegistrar<S>, SourceWrapper<S> {


    /**
     * @return the platform of the module
     */
    Object getPlatform();

    /**
     * Shuts down the platform
     */
    void shutdownPlatform();


    /**
     * The config for imperat
     *
     * @return the config holding all variables.
     */
    ImperatConfig<S> config();

    /**
     * Dispatches and executes a command using {@link Context} only
     *
     * @param context the context
     * @return the usage match result
     */
    @NotNull
    CommandDispatch.Result dispatch(Context<S> context);

    /**
     * Dispatches and executes a command with certain raw arguments
     * using {@link Command}
     *
     * @param source   the sender/executor of this command
     * @param command  the command object to execute
     * @param rawInput the command's args input
     * @return the usage match result
     */
    @NotNull
    CommandDispatch.Result dispatch(S source, Command<S> command, String... rawInput);

    /**
     * Dispatches and executes a command with certain raw arguments
     *
     * @param sender      the sender/executor of this command
     * @param commandName the name of the command to execute
     * @param rawInput    the command's args input
     * @return the usage match result
     */
    CommandDispatch.Result dispatch(S sender, String commandName, String[] rawInput);

    /**
     * Dispatches and executes a command with certain raw arguments
     *
     * @param sender         the sender/executor of this command
     * @param commandName    the name of the command to execute
     * @param rawArgsOneLine the command's args input on ONE LINE
     * @return the usage match result
     */
    CommandDispatch.Result dispatch(S sender, String commandName, String rawArgsOneLine);

    /**
     * Dispatches the full command-line
     *
     * @param sender      the source/sender of the command
     * @param commandLine the command line to dispatch
     * @return the usage match result
     */
    CommandDispatch.Result dispatch(S sender, String commandLine);

    /**
     * @param command the data about the command being written in the chat box
     * @param sender  the sender writing the command
     * @param args    the arguments currently written
     * @return the suggestions at the current position
     */
    CompletableFuture<List<String>> autoComplete(Command<S> command, S sender, String[] args);

    default CompletableFuture<List<String>> autoComplete(Command<S> command, S sender, String argsOneLine) {
        return autoComplete(command, sender, argsOneLine.split(" "));
    }

    /**
     * Debugs all registered commands and their usages.
     *
     * @param treeVisualizing whether to display them in the form of tree
     */
    void debug(boolean treeVisualizing);

}
