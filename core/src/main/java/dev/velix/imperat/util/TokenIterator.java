package dev.velix.imperat.util;

import dev.velix.imperat.exception.TokenParseException;
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
    
    TokenIterator(String input) {
        this.source = input;
    }
    
    public @Nullable Character peek() {
        try {
            return source.charAt(position + 1);
        } catch (StringIndexOutOfBoundsException ex) {
            return null;
        }
    }
    
    @Override
    public boolean hasNext() {
        return peek() != null;
    }
    
    @Override
    public Character next() throws TokenParseException {
        if (!hasNext())
            throw createException();
        
        Character res = peek();
        position++;
        return res;
    }
    
    TokenParseException createException() {
        return new TokenParseException("Buffer overrun while parsing args");
    }
    
    
}
