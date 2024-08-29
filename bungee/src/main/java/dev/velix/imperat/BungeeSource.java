package dev.velix.imperat;

import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public final class BungeeSource implements Source<CommandSender> {

    private final CommandSender sender;
    private final AudienceProvider provider;

    public BungeeSource(AudienceProvider provider, CommandSender sender) {
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

    @Override
    public boolean isConsole() {
        return sender.equals(ProxyServer.getInstance().getConsole());
    }

    @Override
    public void reply(Component component) {
        if (isConsole()) {
            provider.console().sendMessage(component);
        } else
            provider.player(((ProxiedPlayer) sender).getUniqueId());
    }

}