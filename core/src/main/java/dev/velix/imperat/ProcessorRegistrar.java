package dev.velix.imperat;

import dev.velix.imperat.command.processors.CommandPostProcessor;
import dev.velix.imperat.command.processors.CommandPreProcessor;
import dev.velix.imperat.command.processors.CommandProcessingChain;
import dev.velix.imperat.context.Source;

public sealed interface ProcessorRegistrar<S extends Source> permits ImperatConfig {

    /**
     * Sets the whole pre-processing chain
     * @param chain the chain to set
     */
    void setPreProcessorsChain(CommandProcessingChain<S, CommandPreProcessor<S>> chain);

    /**
     * Sets the whole post-processing chain
     * @param chain the chain to set
     */
    void setPostProcessorsChain(CommandProcessingChain<S, CommandPostProcessor<S>> chain);

    /**
     * @return gets the pre-processors in the chain of execution
     * @see CommandPreProcessor
     */
    CommandProcessingChain<S, CommandPreProcessor<S>> getPreProcessors();

    /**
     * @return gets the post-processors in the chain of execution
     * @see CommandPostProcessor
     */
    CommandProcessingChain<S, CommandPostProcessor<S>> getPostProcessors();

}
