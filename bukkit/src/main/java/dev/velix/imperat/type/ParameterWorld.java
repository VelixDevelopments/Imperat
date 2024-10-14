package dev.velix.imperat.type;

import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.type.BaseParameterType;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.UnknownWorldException;
import dev.velix.imperat.util.ImperatDebugger;
import dev.velix.imperat.util.TypeWrap;
import dev.velix.imperat.util.reflection.Reflections;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ParameterWorld extends BaseParameterType<BukkitSource, World> {

    private final static Class<?> WORLD_CLASS = Reflections.getClass("org.bukkit.World");
    private final static Method GET_WORLDS;
    private final static Method GET_NAME;

    static {
        try {
            assert WORLD_CLASS != null;
            GET_WORLDS = Bukkit.class.getMethod("getWorlds");
            GET_NAME = WORLD_CLASS.getMethod("getName");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

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
        return super.matchesInput(input, parameter);
    }

    @Override
    public Collection<String> suggestions() {
        try {
            List<?> worlds = (List<?>) GET_WORLDS.invoke(null);

            return worlds.stream()
                .map((obj) -> {
                    try {
                        return (String) GET_NAME.invoke(obj);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(String::toLowerCase).toList();
        } catch (IllegalAccessException | InvocationTargetException e) {
            ImperatDebugger.error(ParameterWorld.class, "suggestions", e);
            return Collections.emptyList();
        }
    }
}
