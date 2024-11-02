package dev.velix.imperat;

import dev.velix.imperat.command.processors.CommandPostProcessor;
import dev.velix.imperat.command.processors.CommandPreProcessor;
import dev.velix.imperat.context.Source;

import java.util.Queue;

public sealed interface ProcessorRegistrar<S extends Source> permits ImperatConfig {

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
     * @return gets the pre-processors in the chain of execution
     * @see CommandPreProcessor
     */
    Queue<CommandPreProcessor<S>> getPreProcessors();

    /**
     * @return gets the post-processors in the chain of execution
     * @see CommandPostProcessor
     */
    Queue<CommandPostProcessor<S>> getPostProcessors();

}
