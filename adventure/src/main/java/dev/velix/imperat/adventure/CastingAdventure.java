package dev.velix.imperat.adventure;

import net.kyori.adventure.audience.Audience;

public abstract class CastingAdventure<S> implements AdventureProvider<S> {
    
    @Override
    public final Audience audience(S sender) {
        return (Audience) sender;
    }
    
    
}
