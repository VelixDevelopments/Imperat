package dev.zafrias.imperat;

/**
 * Represents an interface that converts an input type of object
 * into another type of object through a defined logic (TODO implement)
 *
 * @param <C> command sender type
 * @param <I> the input object
 * @param <O> the output object
 */
public interface Resolver<C, I, O> {

	/**
	 * @param source the source of the command
	 * @param input  the input object
	 * @return the resolved output from the input object
	 */
	Result<O> resolve(CommandSource<C> source, I input);

}
