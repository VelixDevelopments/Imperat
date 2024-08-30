package dev.velix.imperat;

import dev.velix.imperat.caption.Caption;
import dev.velix.imperat.context.Context;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class BukkitSource implements Source<CommandSender> {

    private final BukkitImperat imperat;
    private final CommandSender sender;
    private final AudienceProvider audiences;

    public BukkitSource(BukkitImperat imperat,
                        CommandSender sender, AudienceProvider audiences) {
        this.imperat = imperat;
        this.sender = sender;
        this.audiences = audiences;
    }

    /**
     * @return name of command source
     */
    @Override
    public String getName() {
        return sender.getName();
    }

    /**
     * @return The original command sender type instance
     */
    @Override
    public CommandSender getOrigin() {
        return sender;
    }

    public Player asPlayer() {
        return as(Player.class);
    }

    /**
     * Replies to the command sender with a string message
     * this message is auto translated into a minimessage
     *
     * @param message the message
     */
    @Override
    public void reply(String message) {
        reply(BukkitImperat.MINI_MESSAGE.deserialize(message));
    }
    
    @Override
    public void reply(Caption<CommandSender> caption, Context<CommandSender> context) {
        reply(caption.getMessage(imperat, context));
    }
    
    @Override
    public void reply(String prefix, Caption<CommandSender> caption, Context<CommandSender> context) {
        reply(prefix + caption.getMessage(imperat, context));
    }
    
    
    public void reply(Component component) {
        Audience audience = isConsole() ? audiences.console()
                : audiences.player(((Player) sender).getUniqueId());
        audience.sendMessage(component);
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
        return (T) getOrigin();
    }

}
