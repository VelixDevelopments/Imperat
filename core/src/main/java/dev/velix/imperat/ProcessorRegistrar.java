package dev.velix.imperat;

import dev.velix.imperat.command.processors.CommandPostProcessor;
import dev.velix.imperat.command.processors.CommandPreProcessor;

public sealed interface ProcessorRegistrar<C> permits Imperat {
	
	/**
	 * Registers a command pre-processor
	 * @param preProcessor the pre-processor to register
	 */
	void registerGlobalPreProcessor(CommandPreProcessor<C> preProcessor);
	
	/**
	 * Registers a command post-processor
	 * @param postProcessor the post-processor to register
	 */
	void registerGlobalPostProcessor(CommandPostProcessor<C> postProcessor);
	
	
	/**
	 * Registers a command pre-processor
	 * @param priority the priority for the processor
	 * @param preProcessor the pre-processor to register
	 */
	void registerGlobalPreProcessor(int priority, CommandPreProcessor<C> preProcessor);
	
	/**
	 * Registers a command post-processor
	 * @param priority the priority for the processor
	 * @param postProcessor the post-processor to register
	 */
	void registerGlobalPostProcessor(int priority, CommandPostProcessor<C> postProcessor);
	
}
