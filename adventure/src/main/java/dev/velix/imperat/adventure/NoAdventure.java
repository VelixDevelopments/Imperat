package dev.velix.imperat.adventure;

import net.kyori.adventure.audience.Audience;

public class NoAdventure<S> implements AdventureProvider<S> {

    @Override
    public Audience audience(S sender) {
        return null;
    }

}
