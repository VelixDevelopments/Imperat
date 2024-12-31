package dev.velix.imperat.type;

import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.command.parameters.type.BaseParameterType;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.util.TypeWrap;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class ParameterColorBungee extends BaseParameterType<BukkitSource, ChatColor> {


    public ParameterColorBungee() {
        super(TypeWrap.of(ChatColor.class));
        withSuggestions(Arrays.stream(ChatColor.values()).map(ChatColor::name).toArray(String[]::new));
    }

    @Override
    public @Nullable ChatColor resolve(ExecutionContext<BukkitSource> context, @NotNull CommandInputStream<BukkitSource> commandInputStream) {
        final String raw = commandInputStream.currentRaw().orElse(null);
        return raw == null ? null : ChatColor.valueOf(raw);
    }

}
