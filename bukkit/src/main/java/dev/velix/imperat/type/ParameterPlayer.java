package dev.velix.imperat.type;

import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.type.BaseParameterType;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.UnknownPlayerException;
import dev.velix.imperat.util.TypeWrap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class ParameterPlayer extends BaseParameterType<BukkitSource, Player> {

    public ParameterPlayer() {
        super(TypeWrap.of(Player.class));
    }

    @Override
    public @Nullable Player resolve(
        ExecutionContext<BukkitSource> context,
        @NotNull CommandInputStream<BukkitSource> commandInputStream
    ) throws ImperatException {
        String raw = commandInputStream.currentRaw();
        if (raw == null) return null;
        if (raw.equalsIgnoreCase("me")) {
            if (context.source().isConsole()) {
                throw new UnknownPlayerException(raw);
            }
            return context.source().asPlayer();
        }
        final Player player = Bukkit.getPlayer(raw.toLowerCase());
        if (player != null) return player;
        throw new UnknownPlayerException(raw);
    }

    @Override
    public boolean matchesInput(String input, CommandParameter<BukkitSource> parameter) {
        return Bukkit.getPlayer(input.toLowerCase()) != null;
    }

    @Override
    public Collection<String> suggestions() {
        return Bukkit.getOnlinePlayers().stream()
            .map(Player::getName).toList();
    }
}
