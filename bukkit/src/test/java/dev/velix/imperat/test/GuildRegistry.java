package dev.velix.imperat.test;

import dev.velix.imperat.util.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public final class GuildRegistry extends Registry<String, Guild> {
    private static GuildRegistry instance;

    public static GuildRegistry getInstance() {
        if (instance == null)
            instance = new GuildRegistry();
        return instance;
    }

    public Optional<Guild> getGuild(String name) {
        return getData(name);
    }

    public void registerGuild(Guild guild) {
        setData(guild.getName(), guild);
    }

    public void removeGuild(Guild guild) {
        removeData(guild.getName());
    }

    public @Nullable Guild getUserGuild(UUID uniqueId) {
        for (Guild guild : getAll()) {
            if (guild.hasMember(uniqueId)) return guild;
        }
        return null;
    }
}
