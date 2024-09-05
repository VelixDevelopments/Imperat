package dev.velix.imperat.adventure;

import net.kyori.adventure.audience.Audience;

public class CastingAdventure<S> implements AdventureProvider<S> {
    
    @Override
    public Audience audience(final S sender) {
        return (Audience) sender;
    }
    
}
