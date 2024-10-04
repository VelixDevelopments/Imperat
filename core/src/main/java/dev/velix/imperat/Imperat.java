package dev.velix.imperat;


import dev.velix.imperat.annotations.base.AnnotationParser;
import dev.velix.imperat.annotations.base.AnnotationReader;
import dev.velix.imperat.annotations.base.AnnotationReplacer;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.tree.CommandDispatch;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.ContextFactory;
import dev.velix.imperat.verification.UsageVerifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Represents the class that handles all
 * commands' registrations and executions
 * It also caches the settings that the user can
 * change or modify in the api.
 *
 * @param <S> the command sender type
 */
@ApiStatus.AvailableSince("1.0.0")
public non-sealed interface Imperat<S extends Source> extends
    ProcessorRegistrar<S>, ResolverRegistrar<S>,
    CommandRegistrar<S>, SourceWrapper<S>,
    CommandHelpHandler<S>, ThrowableHandler<S> {

    /**
     * @return the platform of the module
     */
    Object getPlatform();

    /**
     * Shuts down the platform
     */
    void shutdownPlatform();

    /**
     * @return The command prefix
     */
    String commandPrefix();

    /**
     * @return the factory for creation of
     * command related contexts {@link Context}
     */
    ContextFactory<S> getContextFactory();

    /**
     * sets the context factory {@link ContextFactory} for the contexts
     *
     * @param contextFactory the context factory to set
     */
    void setContextFactory(ContextFactory<S> contextFactory);


    /**
     * Changes the instance of {@link AnnotationParser}
     *
     * @param parser the parser
     */
    @Contract("null->fail")
    void setAnnotationParser(AnnotationParser<S> parser);

    /**
     * Registers a type of annotations so that it can be
     * detected by {@link AnnotationReader} , it's useful as it allows that type of annotation
     * to be recognized as a true Imperat-related annotation to be used in something like checking if a
     * {@link CommandParameter} is annotated and checks for the annotations it has.
     *
     * @param type the type of annotation
     */
    void registerAnnotations(Class<? extends Annotation>... type);

    /**
     * Registers annotation replacer
     *
     * @param type     the type to replace the annotation by
     * @param replacer the replacer
     * @param <A>      the type of annotation to replace
     */
    <A extends Annotation> void registerAnnotationReplacer(
        final Class<A> type,
        final AnnotationReplacer<A> replacer
    );

    /**
     * Sets the usage verifier to a new instance
     *
     * @param usageVerifier the usage verifier to set
     */
    void setUsageVerifier(UsageVerifier<S> usageVerifier);

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
    CompletableFuture<Collection<String>> autoComplete(Command<S> command, S sender, String[] args);

    default CompletableFuture<Collection<String>> autoComplete(Command<S> command, S sender, String argsOneLine) {
        return autoComplete(command, sender, argsOneLine.split(" "));
    }

    /**
     * Debugs all registered commands and their usages.
     *
     * @param treeVisualizing whether to display them in the form of tree
     */
    void debug(boolean treeVisualizing);
}
