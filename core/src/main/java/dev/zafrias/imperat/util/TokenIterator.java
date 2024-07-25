package dev.zafrias.imperat.util;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

/**
 * Represents a class made specifically to
 * tokenize and manage strings
 */
@ApiStatus.Internal
final class TokenIterator implements Iterator<Character> {

	private final String source;
	private int position = -1;


	public TokenIterator(String input) {
		this.source = input;
	}

	public @Nullable Character peek() {
		try {
			return source.charAt(position + 1);
		} catch (ArrayIndexOutOfBoundsException ex) {
			return null;
		}
	}

	@Override
	public boolean hasNext() {
		return peek() != null;
	}

	@Override
	public Character next() throws ArgumentParseException {
		if (!hasNext())
			throw createException("Buffer overrun while parsing args");

		Character res = peek();
		position++;
		return res;
	}

	ArgumentParseException createException(String msg) {
		return new ArgumentParseException(msg, source, position);
	}


}
