package dev.velix.imperat.type;

import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.parameters.type.BaseParameterType;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.UnknownWorldException;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.util.TypeWrap;
import dev.velix.imperat.util.reflection.Reflections;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

public class ParameterWorld extends BaseParameterType<BukkitSource, World> {

    private final static Class<?> WORLD_CLASS = Reflections.getClass("org.bukkit.World");
    private final static Method GET_WORLDS;
    private final static Method GET_NAME;

    static {
        try {
            assert WORLD_CLASS != null;
            GET_WORLDS = Bukkit.class.getMethod("getWorlds");
            GET_WORLDS.setAccessible(true);

            GET_NAME = WORLD_CLASS.getMethod("getName");
            GET_NAME.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public ParameterWorld(Class<?> type) {
        super(TypeWrap.of((Class<World>) type));
    }

    @Override
    public @Nullable World resolve(ExecutionContext<BukkitSource> context, @NotNull CommandInputStream<BukkitSource> commandInputStream) throws ImperatException {
        String raw = commandInputStream.currentRaw().orElse(null);
        if (raw == null) return null;

        World world = Bukkit.getWorld(raw.toLowerCase());
        if (world != null) return world;
        throw new UnknownWorldException(raw);
    }


    /**
     * Returns the suggestion resolver associated with this parameter type.
     *
     * @return the suggestion resolver for generating suggestions based on the parameter type.
     */
    @Override
    @SuppressWarnings("unchecked")
    public SuggestionResolver<BukkitSource> getSuggestionResolver() {
        try {
            return SuggestionResolver.plain(((List<Object>) GET_WORLDS.invoke(null))
                .stream()
                .map((obj) -> {
                    try {
                        return (String) GET_NAME.invoke(obj);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList()
            );
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public @NotNull World fromString(Imperat<BukkitSource> imperat, String input) {
        return Objects.requireNonNull(Bukkit.getWorld(input));
    }
}
