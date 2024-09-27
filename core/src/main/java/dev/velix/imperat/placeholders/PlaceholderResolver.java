package dev.velix.imperat.placeholders;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.NotNull;

public interface PlaceholderResolver<S extends Source> {
    
    /**
     * Resolves a placeholder
     *
     * @param placeHolderId the id for the placeholder
     * @param imperat       the imperat
     * @return the placeholder to return
     */
    @NotNull String resolve(String placeHolderId, Imperat<S> imperat);
    
    
}
