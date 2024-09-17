package dev.velix.exception;

import java.io.Serial;

/**
 * Exception thrown when an error occurs while parsing tokens.
 */
public final class TokenParseException extends RuntimeException {
    
    @Serial
    private static final long serialVersionUID = -8555316116315990226L;
    
    /**
     * Return a new {@link TokenParseException} with the given message, cause, source and position.
     *
     * @param message The message to use for this exception
     */
    public TokenParseException(String message) {
        super(message);
    }
    
    
}
