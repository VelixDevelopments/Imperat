package dev.velix.imperat.examples;

import dev.velix.imperat.BukkitContextResolverFactory;
import dev.velix.imperat.examples.custom_annotations.MyCustomAnnotation2;
import dev.velix.imperat.resolvers.ContextResolver;
import dev.velix.imperat.test.guild.GuildRegistry;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Parameter;

public final class MyContextResolverFactory implements BukkitContextResolverFactory {


    /**
     * Creates a context resolver based on the parameter
     *
     * @param parameter the parameter
     * @return the {@link ContextResolver} specific for that parameter
     */
    @Override
    public @Nullable ContextResolver<CommandSender, ?> create(@Nullable Parameter parameter) {

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
