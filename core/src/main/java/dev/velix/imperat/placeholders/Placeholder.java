package dev.velix.imperat.placeholders;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.NotNull;

public sealed interface Placeholder<S extends Source> permits PlaceholderImpl {
    
    /**
     * The unique name for this placeholder
     *
     * @return the name for this placeholder
     */
    @NotNull String id();
    
    /**
     * The dynamic resolver for this placeholder
     *
     * @return the {@link PlaceholderResolver} resolver
     */
    @NotNull PlaceholderResolver<S> resolver();
    
    boolean isUsedIn(String input);
    
    default String resolveInput(String input, Imperat<S> imperat) {
        return resolver().resolve(input, imperat);
    }
}
