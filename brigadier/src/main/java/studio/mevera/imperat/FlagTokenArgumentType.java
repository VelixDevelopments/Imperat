package studio.mevera.imperat;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import studio.mevera.imperat.util.Patterns;

/**
 * Brigadier argument type backing the per-scope {@code <flag>} node that
 * replaced the per-form flag literal zoo. Consumes a single
 * whitespace-bounded token and accepts it iff the token is ANY flag form —
 * bare ({@code -name} / {@code --name}) or inline assignment
 * ({@code -name=value} / {@code --name=value}).
 *
 * <p>Flags are argument nodes rather than literals on purpose: literal
 * completion happens client-side from the synced tree, so the server can
 * never filter an already-used flag out of a literal's suggestions. With an
 * argument node the client asks the server ({@code ASK_SERVER}) and the
 * Imperat tree suggester decides — used flags filtered, values completed,
 * flags offered before greedy text.</p>
 *
 * <p>Non-flag tokens revert the reader and throw, so sibling positional /
 * literal nodes parse them instead.</p>
 */
public final class FlagTokenArgumentType implements ArgumentType<String> {

    private static final SimpleCommandExceptionType NOT_A_FLAG =
            new SimpleCommandExceptionType(() -> "Token is not a flag form");

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        while (reader.canRead() && reader.peek() != ' ') {
            reader.skip();
        }
        String token = reader.getString().substring(start, reader.getCursor());
        if (token.isEmpty() || !Patterns.isInputFlag(token)) {
            reader.setCursor(start);
            throw NOT_A_FLAG.createWithContext(reader);
        }
        return token;
    }
}
