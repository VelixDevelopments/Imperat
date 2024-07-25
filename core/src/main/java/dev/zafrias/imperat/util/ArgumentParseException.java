package dev.zafrias.imperat.util;

import lombok.Getter;

/**
 * Exception thrown when an error occurs while parsing arguments.
 */
public final class ArgumentParseException extends RuntimeException {

	private static final long serialVersionUID = -8555316116315990226L;

	private final String source;

	/**
	 * Gets the position of the last fetched argument in the provided source
	 * string.
	 */
	@Getter
	private final int position;

	/**
	 * Return a new {@link ArgumentParseException} with the given message, source and position.
	 *
	 * @param message  The message to use for this exception
	 * @param source   The source string being parsed
	 * @param position The current position in the source string
	 */
	public ArgumentParseException(String message, String source, int position) {
		super(message);
		this.source = source;
		this.position = position;
	}

	/**
	 * Return a new {@link ArgumentParseException} with the given message, cause, source and position.
	 *
	 * @param message  The message to use for this exception
	 * @param cause    The cause for this exception
	 * @param source   The source string being parsed
	 * @param position The current position in the source string
	 */
	public ArgumentParseException(String message, Throwable cause, String source, int position) {
		super(message, cause);
		this.source = source;
		this.position = position;
	}

	/**
	 * Return a string pointing to the position of the arguments when this
	 * exception occurs.
	 *
	 * @return The appropriate position string
	 */
	public String getHighlightedPart() {
		int position = this.position;
		if (source.length() > 80 && position >= 37) {
			//int startPos = position - 37;
			//int endPos = Math.min(source.length(), position + 37);
			position -= 40;
		}
		return StringUtils.repeat(" ", position) + "^";
	}

	/**
	 * Returns the source string arguments are being parsed from.
	 *
	 * @return The source string
	 */
	public String getSourceString() {
		return source;
	}

}
