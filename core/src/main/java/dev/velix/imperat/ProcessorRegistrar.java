package dev.velix.imperat;

import dev.velix.imperat.command.processors.CommandPostProcessor;
import dev.velix.imperat.command.processors.CommandPreProcessor;
import dev.velix.imperat.context.Source;

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
	
}
