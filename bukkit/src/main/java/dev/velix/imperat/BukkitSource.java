package dev.velix.imperat;

import dev.velix.imperat.adventure.AdventureProvider;
import dev.velix.imperat.caption.Caption;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;
import net.kyori.adventure.text.ComponentLike;
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
    public String getName() {
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
     * this message is auto translated into a minimessage
     *
     * @param message the message
     */
    @Override
    public void reply(String message) {
        reply(BukkitImperat.MINI_MESSAGE.deserialize(message));
    }

    public void reply(final ComponentLike component) {
        provider.send(this, component);
    }

    @Override
    public <S extends Source> void reply(Caption<S> caption, Context<S> context) {
        reply(caption.getMessage((Imperat<S>) imperat, context));
    }

    @Override
    public <S extends Source> void reply(String prefix, Caption<S> caption, Context<S> context) {
        reply(prefix + caption.getMessage((Imperat<S>) imperat, context));
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
