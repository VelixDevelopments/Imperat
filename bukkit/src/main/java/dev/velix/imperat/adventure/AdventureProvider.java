package dev.velix.imperat.adventure;

import net.kyori.adventure.audience.Audience;

public interface AdventureProvider<S> {

    Audience audience(final S s);
    default void close() {}

}
