package dev.zafrias.imperat.util;

import dev.zafrias.imperat.context.ArgumentQueue;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class StringTokenizer {

	private static final char CHAR_BACKSLASH = '\\';
	private static final char CHAR_SINGLE_QUOTE = '\'';
	private static final char CHAR_DOUBLE_QUOTE = '"';

	public static ArgumentQueue parseToQueue(String argumentsInOneLine) {
		if (argumentsInOneLine.isEmpty())
			return ArgumentQueue.empty();

		TokenIterator iterator = new TokenIterator(argumentsInOneLine);
		ArgumentQueue toCollect = ArgumentQueue.empty();
		while (iterator.hasNext()) {
			Character next = iterator.next();
			if (Character.isWhitespace(next))
				continue;
			toCollect.add(nextArg(iterator));
		}

		return toCollect;
	}

	private static String nextArg(TokenIterator iterator) throws ArgumentParseException {
		StringBuilder argBuilder = new StringBuilder();
		if (iterator.hasNext()) {
			Character character = iterator.peek();
			assert character != null;
			boolean quoted = (character == CHAR_DOUBLE_QUOTE || character == CHAR_SINGLE_QUOTE);
			parseString(iterator, character, argBuilder, quoted);
		}
		return argBuilder.toString();
	}

	private static void parseString(final TokenIterator iterator,
	                                final Character start,
	                                final StringBuilder builder,
	                                final boolean quoted) throws ArgumentParseException {
		// Consume the start quotation character
		Character character = quoted ? iterator.next() : start;

		if (quoted && character != start) {
			throw iterator.createException(
					  String.format("Real next character '%s' is not matching with the expected quotation character '%s'", character, start)
			);
		}

		while (iterator.hasNext()) {
			character = iterator.peek();
			assert character != null;

			if (quoted && character == start) {
				iterator.next();
				return;
			} else if (!quoted && Character.isWhitespace(character))
				return;


			if (character == CHAR_BACKSLASH)
				parseEscape(iterator, builder);
			else
				builder.append(iterator.next());
		}

	}

	private static void parseEscape(TokenIterator state, StringBuilder builder) throws ArgumentParseException {
		state.next(); // Consume '\'
		builder.append(state.next());
	}
}
