package dev.velix.imperat;

import dev.velix.imperat.adventure.AdventureProvider;
import dev.velix.imperat.context.Source;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class BukkitSource implements Source {
    
    private final BukkitImperat imperat;
    private final CommandSender sender;
    private final AdventureProvider<CommandSender> provider;
    
    public BukkitSource(
            final BukkitImperat imperat,
            final CommandSender sender,
            final AdventureProvider<CommandSender> provider
    ) {
        this.imperat = imperat;
        this.sender = sender;
        this.provider = provider;
    }
    
    /**
     * @return name of a command source
     */
    @Override
    public String name() {
        return sender.getName();
    }
    
    /**
     * @return The original command sender type instance
     */
    @Override
    public CommandSender origin() {
        return sender;
    }
    
    public Player asPlayer() {
        return as(Player.class);
    }
    
    /**
     * Replies to the command sender with a string message
     *
     * @param message the message
     */
    @Override
    public void reply(final String message) {
        //check if adventure is loaded, otherwise we send the message normally
        if (imperat.isAdventureLoaded()) {
            imperat.getAdventureProvider().sendMiniMessage(sender, message);
        } else {
            this.sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }
    
    @Override
    public void error(final String message) {
        this.reply(ChatColor.RED + message);
    }
    
    /**
     * Replies to the command sender with a component message
     *
     * @param component the message component
     */
    public void reply(final ComponentLike component) {
        provider.send(this, component);
    }
    
    /**
     * @return Whether the command source is from the console
     */
    @Override
    public boolean isConsole() {
        return !(sender instanceof Player);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T as(Class<T> clazz) {
        return (T) origin();
    }
    
}
