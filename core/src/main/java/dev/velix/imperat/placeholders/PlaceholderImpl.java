package dev.velix.imperat.placeholders;

import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.regex.Pattern;

final class PlaceholderImpl<S extends Source> implements Placeholder<S> {
    private final String id;
    private final PlaceholderResolver<S> resolver;

    private final Pattern pattern;

    PlaceholderImpl(String id, PlaceholderResolver<S> resolver) {
        this.id = id;
        this.resolver = resolver;
        this.pattern = Pattern.compile(id);
    }


    @Override
    public @NotNull String id() {
        return id;
    }

    @Override
    public @NotNull PlaceholderResolver<S> resolver() {
        return resolver;
    }

    @Override
    public boolean isUsedIn(String input) {
        return pattern.matcher(input).matches();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PlaceholderImpl<?>) obj;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
