package dev.velix.examples;

import dev.velix.BukkitContextResolverFactory;
import dev.velix.BukkitSource;
import dev.velix.annotations.base.element.ParameterElement;
import dev.velix.examples.custom_annotations.MyCustomAnnotation2;
import dev.velix.resolvers.ContextResolver;
import dev.velix.test.guild.GuildRegistry;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public final class MyContextResolverFactory implements BukkitContextResolverFactory {
    
    
    /**
     * Creates a context resolver based on the parameter
     *
     * @param parameter the parameter
     * @return the {@link ContextResolver} specific for that parameter
     */
    @Override
    public @Nullable ContextResolver<BukkitSource, ?> create(@Nullable ParameterElement parameter) {
        
        if (parameter == null || !parameter.isAnnotationPresent(MyCustomAnnotation2.class)) {
            return null;
        }
        
        return (context, methodParam) -> {
            var source = context.getSource();
            if (source.isConsole()) return null;
            return GuildRegistry.getInstance()
                    .getUserGuild(source.as(Player.class).getUniqueId());
        };
    }
    
}
