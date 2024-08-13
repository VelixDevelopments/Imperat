package dev.velix.imperat.annotations.loaders;

import dev.velix.imperat.command.Command;
import org.jetbrains.annotations.Nullable;

/**
 * This class represents an object that is loaded from an annotation
 *
 * @param <T> type of the object that can be annotated
 */
@Deprecated
public interface AnnotationLoader<C, T> {

	/**
	 * Loads the object using the annotation
	 *
	 * @param alreadyLoaded the already loaded object
	 * @return the object loaded from the annotation
	 */
	@Nullable
	T load(@Nullable Command<C> alreadyLoaded);

}
