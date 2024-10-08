package dev.velix.imperat.context;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

@ApiStatus.Internal
final class ArgumentQueueImpl extends LinkedList<String> implements ArgumentQueue {

    private final List<String> unmodifiableView;

    ArgumentQueueImpl(@NotNull Collection<? extends String> input) {
        super(input);
        this.unmodifiableView = Collections.unmodifiableList(this);
    }


    public ArgumentQueueImpl(@NotNull String... rawArgs) {
        Collections.addAll(this, rawArgs);
        this.unmodifiableView = Collections.unmodifiableList(this);
    }

    @Override
    public @NotNull String join(String delimiter) {
        return String.join(delimiter, this);
    }

    @Override
    public @NotNull String join(@NotNull String delimiter, int startIndex) {
        StringJoiner joiner = new StringJoiner(delimiter);
        for (int i = startIndex; i < this.size(); i++) {
            joiner.add(get(i));
        }
        return joiner.toString();
    }

    @Override
    public @NotNull @UnmodifiableView List<String> asImmutableView() {
        return unmodifiableView;
    }

    @Override
    public @NotNull @Unmodifiable List<String> asImmutableCopy() {
        return Collections.unmodifiableList(new LinkedList<>(this));
    }

    @Override
    public @NotNull ArgumentQueue copy() {
        return new ArgumentQueueImpl(this);
    }

}
