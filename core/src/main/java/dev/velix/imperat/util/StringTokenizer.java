package dev.velix.imperat.util;

import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.exceptions.TokenParseException;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class StringTokenizer {
	
	private static final char CHAR_BACKSLASH = '\\';
	private static final char CHAR_SINGLE_QUOTE = '\'';
	private static final char CHAR_DOUBLE_QUOTE = '"';
	
	private StringTokenizer() {}
	
	public static ArgumentQueue parseToQueue(String argumentsInOneLine) {
		if (argumentsInOneLine.isEmpty())
			return ArgumentQueue.empty();
		
		TokenIterator iterator = new TokenIterator(argumentsInOneLine);
		ArgumentQueue toCollect = ArgumentQueue.empty();
		while (iterator.hasNext()) {
			Character next = iterator.next();
			if (Character.isWhitespace(next))
				continue;
			
			String arg = nextArg(iterator, next);
			toCollect.add(arg);
		}
		
		return toCollect;
	}
	
	private static String nextArg(TokenIterator iterator, Character character) throws TokenParseException {
		StringBuilder argBuilder = new StringBuilder();
		if (iterator.hasNext()) {
			assert character != null;
			boolean quoted = (character == CHAR_DOUBLE_QUOTE || character == CHAR_SINGLE_QUOTE);
			parseString(iterator, character, argBuilder, quoted);
		}
		//group member setperm test
		return argBuilder.toString();
	}
	
	private static void parseString(final TokenIterator iterator,
	                                final Character start,
	                                final StringBuilder builder,
	                                final boolean quoted) throws TokenParseException {
		// Consume the start quotation character
		Character character = quoted ? iterator.next() : start;
		
		builder.append(character);
		
		while (iterator.hasNext()) {
			assert character != null;
			character = iterator.next();
			
			if ((quoted && character == start)
							|| (!quoted && Character.isWhitespace(character))) {
				break;
			}
			
			if (character == CHAR_BACKSLASH)
				parseEscape(iterator, builder);
			else
				builder.append(character);
		}
		
	}
	
	private static void parseEscape(TokenIterator state, StringBuilder builder) throws TokenParseException {
		builder.append(state.next());
	}
}
