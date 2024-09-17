package dev.velix;

import dev.velix.command.processors.CommandPostProcessor;
import dev.velix.command.processors.CommandPreProcessor;
import dev.velix.context.Source;

public sealed interface ProcessorRegistrar<S extends Source> permits Imperat {
    
    /**
     * Registers a command pre-processor
     *
     * @param preProcessor the pre-processor to register
     */
    void registerGlobalPreProcessor(CommandPreProcessor<S> preProcessor);
    
    /**
     * Registers a command post-processor
     *
     * @param postProcessor the post-processor to register
     */
    void registerGlobalPostProcessor(CommandPostProcessor<S> postProcessor);
    
    
    /**
     * Registers a command pre-processor
     *
     * @param priority     the priority for the processor
     * @param preProcessor the pre-processor to register
     */
    void registerGlobalPreProcessor(int priority, CommandPreProcessor<S> preProcessor);
    
    /**
     * Registers a command post-processor
     *
     * @param priority      the priority for the processor
     * @param postProcessor the post-processor to register
     */
    void registerGlobalPostProcessor(int priority, CommandPostProcessor<S> postProcessor);
    
}
