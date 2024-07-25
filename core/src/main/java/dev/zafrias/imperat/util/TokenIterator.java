package dev.zafrias.imperat.util;

/**
 * Represents a class made specifically to
 * tokenize and manage strings
 */
final class Tokenizer {

	private final String source;
	private final char[] input;
	private int position = -1;

	private static final int BACKSLASH = '\\';
	private static final int SINGLE_QUOTE = '\'';
	private static final int DOUBLE_QUOTE = '"';

	public Tokenizer(String input) {
		this.source = input;
		this.input = input.toCharArray();
	}


	public boolean hasNext() {
		return (position+1) < input.length;
	}

	public int next() throws ArgumentParseException {
		if (!hasNext())
			throw createException("Buffer overrun while parsing args");

		position++;
		return input[position];
	}

	private ArgumentParseException createException(String msg) {
		return new ArgumentParseException(msg, source, position);
	}


}
