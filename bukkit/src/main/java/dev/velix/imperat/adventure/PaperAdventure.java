package dev.velix.imperat.adventure;

import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;

public class PaperAdventure implements AdventureProvider<CommandSender> {

    @Override
    public Audience audience(final CommandSender sender) {
        return (Audience) sender;
    }

}
