package dev.velix.imperat.context;

import dev.velix.imperat.context.internal.SortedArgumentQueue;
import dev.velix.imperat.util.StringTokenizer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Deque;
import java.util.List;

/**
 * Represents a custom data structure made specifically
 * for handling the arguments entered by the {@link Source}
 */
@ApiStatus.AvailableSince("1.0.0")
public interface ArgumentQueue extends Deque<String>, List<String>, Cloneable {

    /**
     * Joins all present arguments in this stack
     *
     * @param delimiter Delimiter between these arguments.
     * @return The combined string
     */
    @NotNull
    String join(String delimiter);

    /**
     * Joins all present arguments in this stack, starting from
     * the specified index
     *
     * @param delimiter  Delimiter between these arguments
     * @param startIndex The start index to combine from
     * @return The combined string
     */
    @NotNull
    String join(@NotNull String delimiter, int startIndex);


    /**
     * Returns this argument stack as an immutable view. This can be therefore
     * passed to any conditions or resolvers without having to worry about being
     * unintentionally modified.
     * <p>
     * Note that this does not create an independent copy, and instead returns
     * a view which does not allow modifications. If this argument stack gets
     * modified from somewhere else, the immutable view will also be modified.
     *
     * @return The argument stack as an immutable view
     */
    @NotNull
    @UnmodifiableView
    List<String> asImmutableView();

    /**
     * Returns an immutable copy of this stack. This copy will behave
     * independently of the original {@link ArgumentQueue}.
     *
     * @return An immutable copy of this {@link ArgumentQueue}.
     */
    @NotNull
    @Unmodifiable
    List<String> asImmutableCopy();

    /**
     * Returns an independent copy of this argument stack.
     *
     * @return A copy of this argument stack
     */
    @NotNull
    ArgumentQueue copy();

    static ArgumentQueue parse(String[] rawArguments) {
        return StringTokenizer.parseToQueue(String.join(" ", rawArguments));
    }

    static ArgumentQueue parse(String string) {
        return StringTokenizer.parseToQueue(string);
    }
    
    static ArgumentQueue parseAutoCompletion(String[] rawArguments) {
        return parseAutoCompletion(String.join(" ", rawArguments));
    }
    
    static ArgumentQueue parseAutoCompletion(String string) {
        if (string.isEmpty()) {
            return StringTokenizer.parseToQueue(" ");
        }
        return parse(string);
    }

    /**
     * @return a new, empty {@link ArgumentQueue}.
     */
    static ArgumentQueue empty() {
        return new SortedArgumentQueue();
    }

}
