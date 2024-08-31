package dev.velix.imperat;

import dev.velix.imperat.command.processors.CommandPostProcessor;
import dev.velix.imperat.command.processors.CommandPreProcessor;

public sealed interface ProcessorRegistrar<C> permits Imperat {
	
	/**
	 * Registers a command pre-processor
	 * @param preProcessor the pre-processor to register
	 */
	void registerPreProcessor(CommandPreProcessor<C> preProcessor);
	
	/**
	 * Registers a command post-processor
	 * @param postProcessor the post-processor to register
	 */
	void registerPostProcessor(CommandPostProcessor<C> postProcessor);
	
	
	/**
	 * Registers a command pre-processor
	 * @param priority the priority for the processor
	 * @param preProcessor the pre-processor to register
	 */
	void registerPreProcessor(int priority, CommandPreProcessor<C> preProcessor);
	
	/**
	 * Registers a command post-processor
	 * @param priority the priority for the processor
	 * @param postProcessor the post-processor to register
	 */
	void registerPostProcessor(int priority, CommandPostProcessor<C> postProcessor);
	
}
