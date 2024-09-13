package dev.velix.imperat.adventure;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public final class BukkitAdventure implements AdventureProvider<CommandSender> {
    
    public static final EmptyBukkitAdventure EMPTY = new EmptyBukkitAdventure();
    
    private final BukkitAudiences audiences;
    
    public BukkitAdventure(final Plugin plugin) {
        this.audiences = BukkitAudiences.create(plugin);
    }
    
    @Override
    public Audience audience(final CommandSender sender) {
        return audiences.sender(sender);
    }
    
    @Override
    public void close() {
        this.audiences.close();
    }
    
}
