package dev.velix.imperat;


import dev.velix.imperat.annotations.AnnotationParser;
import dev.velix.imperat.annotations.AnnotationReplacer;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.internal.ContextFactory;
import dev.velix.imperat.verification.UsageVerifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Represents the class that handles all
 * commands' registrations and executions
 * It also caches the settings that the user can
 * change or modify in the api.
 *
 * @param <C> the command sender type
 */
@ApiStatus.AvailableSince("1.0.0")
public non-sealed interface Imperat<C> extends
        ProcessorRegistrar<C>, ResolverRegistrar<C>,
        CommandRegistrar<C>, CaptionRegistrar<C>,
        SourceWrapper<C>, CommandHelpHandler<C> {
    
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
    ContextFactory<C> getContextFactory();

    /**
     * sets the context factory {@link ContextFactory} for the contexts
     *
     * @param contextFactory the context factory to set
     */
    void setContextFactory(ContextFactory<C> contextFactory);
    
    
    /**
     * Changes the instance of {@link AnnotationParser}
     * @param parser the parser
     */
    @Contract("null->fail")
    void setAnnotationParser(AnnotationParser<C> parser);
    
    
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
    void setUsageVerifier(UsageVerifier<C> usageVerifier);
    
    
    /**
     * Dispatches and executes a command with certain raw arguments
     *
     * @param sender      the sender/executor of this command
     * @param commandName the name of the command to execute
     * @param rawInput    the command's args input
     */
    void dispatch(C sender, String commandName, String... rawInput);

    /**
     * Dispatches and executes a command with certain raw arguments
     *
     * @param sender         the sender/executor of this command
     * @param commandName    the name of the command to execute
     * @param rawArgsOneLine the command's args input
     */
    void dispatch(C sender, String commandName, String rawArgsOneLine);
    
    /**
     * @param command the data about the command being written in the chat box
     * @param sender  the sender writing the command
     * @param args    the arguments currently written
     * @return the suggestions at the current position
     */
    List<String> autoComplete(Command<C> command, C sender, String[] args);
    
}
