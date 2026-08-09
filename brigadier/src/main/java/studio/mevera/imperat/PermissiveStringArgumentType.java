package studio.mevera.imperat;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

/**
 * Brigadier {@link ArgumentType} that consumes a single whitespace-delimited
 * token with <b>no character restrictions</b>.
 *
 * <p>Brigadier's stock {@code StringArgumentType.string()} internally falls
 * through to {@link StringReader#readUnquotedString()} for unquoted input,
 * which only permits the charset {@code [0-9A-Za-z._\-+]}. Characters such
 * as {@code *}, {@code ?}, {@code =}, or {@code %} are rejected — the parser
 * stops at them and leaves trailing data, producing a client-side syntax
 * error even when the server-side type would accept them.</p>
 *
 * <p>This type replaces the stock {@code string()} fallback in
 * {@link BaseBrigadierManager} for non-greedy positional arguments, filler
 * chain nodes, and default flag value nodes. It reads any character until
 * whitespace, matching the same single-token contract as
 * {@code StringArgumentType.string()} but without disallowing any character.
 * Quoted string support ({@code "..."} / {@code '...'}) with backslash
 * escaping is preserved for backward compatibility.</p>
 *
 * <p>Greedy arguments ({@code @Greedy}) continue to use
 * {@code StringArgumentType.greedyString()} since that type already accepts
 * all remaining input without character restrictions.</p>
 */
public final class PermissiveStringArgumentType implements ArgumentType<String> {

    private static final SimpleCommandExceptionType EMPTY_STRING =
            new SimpleCommandExceptionType(() -> "Expected a value but found nothing");

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        if (!reader.canRead()) {
            throw EMPTY_STRING.createWithContext(reader);
        }

        char next = reader.peek();
        if (next == '"') {
            reader.skip();
            return readStringUntil(reader, '"');
        }
        if (next == '\'') {
            reader.skip();
            return readStringUntil(reader, '\'');
        }

        int start = reader.getCursor();
        while (reader.canRead() && reader.peek() != ' ') {
            reader.skip();
        }
        return reader.getString().substring(start, reader.getCursor());
    }

    private static String readStringUntil(StringReader reader, char terminator) throws CommandSyntaxException {
        StringBuilder result = new StringBuilder();
        while (reader.canRead()) {
            char c = reader.read();
            if (c == '\\' && reader.canRead()) {
                char next = reader.read();
                if (next == terminator || next == '\\') {
                    result.append(next);
                } else {
                    result.append(c).append(next);
                }
            } else if (c == terminator) {
                return result.toString();
            } else {
                result.append(c);
            }
        }
        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedEndOfQuote().createWithContext(reader);
    }
}
