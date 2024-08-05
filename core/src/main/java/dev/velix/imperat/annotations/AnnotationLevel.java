package dev.velix.imperat.annotations;

import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a place where  the annotations
 * can be place
 */
@ApiStatus.Internal
public enum AnnotationLevel {

	CLASS,

	METHOD,

	METHOD_PARAMETER;

}
