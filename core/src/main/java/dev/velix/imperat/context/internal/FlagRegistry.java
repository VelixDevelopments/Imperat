package dev.velix.imperat.context.internal;

import dev.velix.imperat.util.Registry;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class FlagRegistry extends Registry<String, ResolvedFlag> {
    
    public FlagRegistry() {
        super();
    }
    
}
