package dev.velix.imperat;

import dev.velix.imperat.adventure.AdventureProvider;
import dev.velix.imperat.caption.Caption;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;
import net.kyori.adventure.text.ComponentLike;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

@SuppressWarnings("unchecked")
public final class BungeeSource implements Source {
    
    private final BungeeImperat imperat;
    private final CommandSender sender;
    private final AdventureProvider<CommandSender> provider;
    
    public BungeeSource(BungeeImperat imperat, AdventureProvider<CommandSender> provider, CommandSender sender) {
        this.imperat = imperat;
        this.provider = provider;
        this.sender = sender;
    }
    
    @Override
    public String name() {
        return sender.getName();
    }
    
    @Override
    public CommandSender origin() {
        return sender;
    }
    
    @Override
    public void reply(String message) {
        sender.sendMessage(TextComponent.fromLegacyText(message));
    }
    
    @Override
    public void error(final String message) {
        this.sender.sendMessage(ChatColor.RED + message);
    }
    
    public void reply(BaseComponent... message) {
        sender.sendMessage(message);
    }
    
    public void reply(final ComponentLike component) {
        provider.send(this, component);
    }
    
    /**
     * Replies to the command sender with a caption message
     *
     * @param caption the {@link Caption} to send
     * @param context the {@link Context} to use
     */
    @Override
    public <S extends Source> void reply(Caption<S> caption, Context<S> context) {
        reply(caption.getMessage((Imperat<S>) imperat, context));
    }
    
    /**
     * Replies to command sender with a caption message
     *
     * @param prefix  the prefix before the caption message
     * @param caption the caption
     * @param context the context
     */
    @Override
    public <S extends Source> void reply(String prefix, Caption<S> caption, Context<S> context) {
        reply(prefix + caption.getMessage((Imperat<S>) imperat, context));
    }
    
    @Override
    public boolean isConsole() {
        return ProxyServer.getInstance().getConsole().equals(sender);
    }
    
}