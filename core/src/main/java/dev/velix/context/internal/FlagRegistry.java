package dev.velix.context.internal;

import dev.velix.util.Registry;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
final class FlagRegistry extends Registry<String, ResolvedFlag> {
    
    public FlagRegistry() {
        super();
    }
    
}
