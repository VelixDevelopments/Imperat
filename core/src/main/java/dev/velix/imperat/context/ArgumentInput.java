package dev.velix.imperat.context;

import dev.velix.imperat.annotations.ContextResolved;
import dev.velix.imperat.util.StringUtils;
import org.jetbrains.annotations.*;

import java.util.Deque;
import java.util.List;

/**
 * Represents a custom data structure made specifically
 * for handling the arguments entered by the {@link Source}
 */
@ApiStatus.AvailableSince("1.0.0")
@ContextResolved
public interface ArgumentInput extends Deque<String>, List<String>, Cloneable {

    static ArgumentInput parse(String[] rawArguments) {
        return StringUtils.parseToQueue(String.join(" ", rawArguments), false);
    }

    static ArgumentInput parse(String string) {
        return StringUtils.parseToQueue(string, false);
    }

    static ArgumentInput parseAutoCompletion(String[] argumentsOnly, boolean extraLastSpace) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < argumentsOnly.length; i++) {
            var arg = argumentsOnly[i];
            builder.append(arg);
            if (!extraLastSpace) {
                if (i != argumentsOnly.length - 1)
                    builder.append(" ");
            } else
                builder.append(" ");
        }
        return parseAutoCompletion(builder.toString(), extraLastSpace);
    }

    static ArgumentInput parseAutoCompletion(String string, boolean extraLastSpace) {
        if (string.isEmpty()) {
            return StringUtils.parseToQueue("", true);
        }
        return StringUtils.parseToQueue(string, true, extraLastSpace);
    }

    /**
     * @return a new, empty {@link ArgumentInput}.
     */
    static ArgumentInput of(String originalLine) {
        return new ArgumentInputImpl(originalLine);
    }

    /**
     * @return a new, empty {@link ArgumentInput}.
     */
    static ArgumentInput empty() {
        return new ArgumentInputImpl();
    }

    String getOriginalRaw();

    /**
     * Fetches the element at the specified index
     *
     * @param index the index
     * @param def   the default element
     * @return the element at the specified index.
     */
    default @Nullable String getOr(int index, @Nullable String def) {
        if (index < 0 || index >= size()) {
            return def;
        }
        return get(index);
    }

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
     * independently of the original {@link ArgumentInput}.
     *
     * @return An immutable copy of this {@link ArgumentInput}.
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
    ArgumentInput copy();

}
