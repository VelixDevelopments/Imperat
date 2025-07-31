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
final class ArgumentInputImpl extends LinkedList<String> implements ArgumentInput {

    private final String originalRaw;
    private final List<String> unmodifiableView;

    ArgumentInputImpl(String originalRaw, @NotNull Collection<? extends String> input) {
        super(input);
        this.originalRaw = originalRaw;
        this.unmodifiableView = Collections.unmodifiableList(this);
    }


    ArgumentInputImpl(String originalRaw, @NotNull String... rawArgs) {
        this.originalRaw = originalRaw;
        Collections.addAll(this, rawArgs);
        this.unmodifiableView = Collections.unmodifiableList(this);
    }

    ArgumentInputImpl() {
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
    public @NotNull ArgumentInput copy() {
        return new ArgumentInputImpl(originalRaw, this);
    }

}
