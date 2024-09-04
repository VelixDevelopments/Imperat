package dev.velix.imperat;

import dev.velix.imperat.adventure.AdventureProvider;
import dev.velix.imperat.caption.Caption;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;
import net.kyori.adventure.text.ComponentLike;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;

public final class BungeeSource implements Source<CommandSender> {

    private final BungeeImperat imperat;
    private final CommandSender sender;
    private final AdventureProvider<CommandSender> provider;

    public BungeeSource(BungeeImperat imperat, AdventureProvider<CommandSender> provider, CommandSender sender) {
        this.imperat = imperat;
        this.provider = provider;
        this.sender = sender;
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public CommandSender getOrigin() {
        return sender;
    }

    @Override
    public void reply(String message) {
        sender.sendMessage(TextComponent.fromLegacyText(message));
    }
    
    /**
     * Replies to the command sender with a caption message
     *
     * @param caption the {@link Caption} to send
     * @param context the {@link Context} to use
     */
    @Override
    public void reply(Caption<CommandSender> caption, Context<CommandSender> context) {
        reply(caption.getMessage(imperat, context));
    }
    
    /**
     * Replies to command sender with a caption message
     *
     * @param prefix  the prefix before the caption message
     * @param caption the caption
     * @param context the context
     */
    @Override
    public void reply(String prefix, Caption<CommandSender> caption, Context<CommandSender> context) {
        reply(prefix + caption.getMessage(imperat, context));
    }
    
    @Override
    public boolean isConsole() {
        return sender.equals(ProxyServer.getInstance().getConsole());
    }

    public void reply(final ComponentLike component) {
        provider.audience(sender).sendMessage(component);
    }

}