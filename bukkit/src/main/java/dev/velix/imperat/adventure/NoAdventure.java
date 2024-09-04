package dev.velix.imperat.adventure;

import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;

public class NoAdventure implements AdventureProvider<CommandSender> {

    @Override
    public Audience audience(CommandSender sender) {
        return null;
    }

}
