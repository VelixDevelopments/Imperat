package dev.velix.imperat.adventure;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;

public final class PaperAdventure extends CastingAdventure<CommandSender> {
    
    private final MiniMessage miniMessage = MiniMessage.builder()
            .tags(
                    TagResolver
                            .builder()
                            .resolver(TagResolver.standard())
                            .build()
            )
            .build();
    
    @Override
    public MiniMessage getMiniMessage() {
        return miniMessage;
    }
}
