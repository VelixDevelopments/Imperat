package dev.velix.imperat.adventure;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public final class BungeeAdventure implements AdventureProvider<CommandSender> {
    
    private final BungeeAudiences audiences;
    
    private final MiniMessage miniMessage = MiniMessage.builder()
            .tags(
                    TagResolver
                            .builder()
                            .resolver(TagResolver.standard())
                            .build()
            )
            .build();
    
    public final static EmptyBungeeAdventure EMPTY = new EmptyBungeeAdventure();
    
    public BungeeAdventure(final Plugin plugin) {
        this.audiences = BungeeAudiences.create(plugin);
    }
    
    @Override
    public Audience audience(final CommandSender sender) {
        return audiences.sender(sender);
    }
    
    @Override
    public MiniMessage getMiniMessage() {
        return miniMessage;
    }
    
    @Override
    public void close() {
        this.audiences.close();
    }
    
}
