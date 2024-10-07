package dev.velix.imperat.type;

import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.type.BaseParameterType;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.UnknownWorldException;
import dev.velix.imperat.util.TypeWrap;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class ParameterWorld extends BaseParameterType<BukkitSource, World> {

    public ParameterWorld() {
        super(TypeWrap.of(World.class));
    }

    @Override
    public @Nullable World resolve(ExecutionContext<BukkitSource> context, @NotNull CommandInputStream<BukkitSource> commandInputStream) throws ImperatException {
        String raw = commandInputStream.currentRaw();
        if (raw == null) return null;

        World world = Bukkit.getWorld(raw.toLowerCase());
        if (world != null) return world;
        throw new UnknownWorldException(raw);
    }

    @Override
    public boolean matchesInput(String input, CommandParameter<BukkitSource> parameter) {
        return Bukkit.getWorld(input) != null;
    }

    @Override
    public Collection<String> suggestions() {
        return Bukkit.getWorlds().stream()
            .map(World::getName)
            .map(String::toLowerCase).toList();
    }
}
