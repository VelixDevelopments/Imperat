package dev.velix.imperat;

import dev.velix.imperat.context.Source;
import net.kyori.adventure.text.ComponentLike;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public final class BungeeSource implements Source {
    
    private final BungeeImperat imperat;
    private final CommandSender sender;
    
    public BungeeSource(BungeeImperat imperat, CommandSender sender) {
        this.imperat = imperat;
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
        if (imperat.isAdventureLoaded() /*&& imperat.isMiniMessageLoaded()*/) {
            imperat.getAdventureProvider().sendMiniMessage(sender, message);
        } else {
            sender.sendMessage(TextComponent.fromLegacyText(message));
        }
    }
    
    @Override
    public void error(final String message) {
        reply(ChatColor.RED + message);
    }
    
    public void reply(BaseComponent... message) {
        sender.sendMessage(message);
    }
    
    public void reply(final ComponentLike component) {
        imperat.getAdventureProvider().send(this, component);
    }
    
    @Override
    public boolean isConsole() {
        return ProxyServer.getInstance().getConsole().equals(sender);
    }
    
}