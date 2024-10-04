package dev.velix.imperat.placeholders;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.util.Preconditions;
import org.jetbrains.annotations.NotNull;

public sealed interface Placeholder<S extends Source> permits PlaceholderImpl {

    /**
     * The unique name for this placeholder
     *
     * @return the name for this placeholder
     */
    @NotNull
    String id();

    /**
     * The dynamic resolver for this placeholder
     *
     * @return the {@link PlaceholderResolver} resolver
     */
    @NotNull
    PlaceholderResolver<S> resolver();

    boolean isUsedIn(String input);

    default String resolveInput(String id, Imperat<S> imperat) {
        return resolver().resolve(id, imperat);
    }

    String replaceResolved(Imperat<S> imperat, String id, String input);

    static <S extends Source> Builder<S> builder(String id) {
        return new Builder<>(id);
    }


    final class Builder<S extends Source> {
        private final String id;
        private PlaceholderResolver<S> resolver = null;

        Builder(String id) {
            this.id = id;
        }

        public Builder<S> resolver(PlaceholderResolver<S> resolver) {
            this.resolver = resolver;
            return this;
        }

        public Placeholder<S> build() {
            Preconditions.notNull(resolver, "resolver is not set in the placeholder-builder");
            return new PlaceholderImpl<>(id, resolver);
        }
    }
}
