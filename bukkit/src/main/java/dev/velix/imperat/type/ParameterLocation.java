package dev.velix.imperat.type;

import static dev.velix.imperat.exception.SourceException.ErrorLevel.SEVERE;

import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.command.parameters.type.BaseParameterType;
import dev.velix.imperat.command.parameters.type.ParameterType;
import dev.velix.imperat.command.parameters.type.ParameterTypes;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.SourceException;
import dev.velix.imperat.util.TypeUtility;
import dev.velix.imperat.util.TypeWrap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unchecked")
public class ParameterLocation extends BaseParameterType<BukkitSource, Location> {

    private final static String SINGLE_STRING_SEPARATOR = ";";

    private final ParameterType<BukkitSource, Double> doubleParser;
    public ParameterLocation() {
        super(TypeWrap.of(Location.class));
        doubleParser = ParameterTypes.numeric(Double.class);
    }

    @Override
    public @Nullable Location resolve(@NotNull ExecutionContext<BukkitSource> context, @NotNull CommandInputStream<BukkitSource> stream, String input) throws ImperatException {



        try {
            String currentRaw = stream.currentRaw().orElseThrow();
            return locFromStr(context, stream, currentRaw);
        }catch (Exception ex) {

            World world;

            String currentRaw = stream.currentRaw().orElseThrow();
            if(!TypeUtility.isNumber(currentRaw) && Bukkit.getWorld(currentRaw) != null) {
                world = Bukkit.getWorld(currentRaw);
                stream.skipRaw();
            }else if(context.source().isConsole()) {
                var worlds = Bukkit.getWorlds();
                if(worlds.isEmpty()) {
                    //worlds may be empty.
                    throw new SourceException(SEVERE, "Failed to fetch the world of the given location");
                }
                world = Bukkit.getWorlds().get(0);
            }else {
                world = context.source().asPlayer().getWorld();
            }

            ParameterType<BukkitSource, Double> doubleParser = (ParameterType<BukkitSource, Double>) context.imperatConfig().getParameterType(Double.class);
            if(doubleParser == null) {
                throw new SourceException(SEVERE, "Failed to find a parser for type '%s'", Double.class.getTypeName());
            }
            Double x = doubleParser.resolve(context, stream, stream.readInput());
            if(x == null ) throw new SourceException(SEVERE, "X coordinate is invalid");
            stream.skipRaw();

            Double y = doubleParser.resolve(context, stream, stream.readInput());
            if(y == null ) throw new SourceException(SEVERE, "Y coordinate is invalid");
            stream.skipRaw();

            Double z = doubleParser.resolve(context, stream, stream.readInput());
            if(z == null) throw new SourceException(SEVERE, "Z coordinate is invalid");

            return new Location(world, x, y, z);
        }

    }

    private @NotNull Location locFromStr(ExecutionContext<BukkitSource> context, CommandInputStream<BukkitSource> stream, String currentRaw) throws ImperatException {
        String[] split = currentRaw.split(SINGLE_STRING_SEPARATOR);
        if(split.length < 4) {
            throw new IllegalArgumentException();
        }

        World world = Bukkit.getWorld(split[0]);
        if(world == null) {
            throw new IllegalArgumentException();
        }

        Double x = doubleParser.resolve(context, stream, split[1]);
        Double y = doubleParser.resolve(context, stream, split[2]);
        Double z = doubleParser.resolve(context, stream, split[3]);

        if(x == null || y == null || z == null) {
            throw new SourceException(SEVERE, "Failed to parse location from input '%s'", currentRaw);
        }

        return new Location(world, x, y, z);
    }

}
