package dev.velix.imperat.context;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;

@ApiStatus.Internal
final class ArgumentQueueImpl extends LinkedList<String> implements ArgumentQueue {

    private final String originalRaw;
    private final List<String> unmodifiableView;

    ArgumentQueueImpl(String originalRaw, @NotNull Collection<? extends String> input) {
        super(input);
        this.originalRaw = originalRaw;
        this.unmodifiableView = Collections.unmodifiableList(this);
    }


    ArgumentQueueImpl(String originalRaw, @NotNull String... rawArgs) {
        this.originalRaw = originalRaw;
        Collections.addAll(this, rawArgs);
        this.unmodifiableView = Collections.unmodifiableList(this);
    }

    ArgumentQueueImpl() {
        this.originalRaw = "";
        this.unmodifiableView = new ArrayList<>();
    }

    @Override
    public String getOriginalRaw() {
        return originalRaw;
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
        return new ArgumentQueueImpl(originalRaw, this);
    }

}
