package dev.velix.imperat.type;

import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.command.parameters.type.BaseParameterType;
import dev.velix.imperat.command.parameters.type.ParameterType;
import dev.velix.imperat.command.parameters.type.ParameterTypes;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.InvalidLocationFormatException;
import dev.velix.imperat.exception.UnknownWorldException;
import dev.velix.imperat.util.TypeUtility;
import dev.velix.imperat.util.TypeWrap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

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
                    throw new InvalidLocationFormatException(input, InvalidLocationFormatException.Reason.NO_WORLDS_AVAILABLE);
                }
                world = Bukkit.getWorlds().get(0);
            }else {
                world = context.source().asPlayer().getWorld();
            }

            ParameterType<BukkitSource, Double> doubleParser = (ParameterType<BukkitSource, Double>) context.imperatConfig().getParameterType(Double.class);
            if(doubleParser == null) {
                throw new IllegalArgumentException("Failed to find a parser for type '" + Double.class.getTypeName() + "'");
            }

            String inputX = stream.readInput();
            Double x = doubleParser.resolve(context, stream, inputX);
            if(x == null ) throw new InvalidLocationFormatException(input, InvalidLocationFormatException.Reason.INVALID_X_COORD, inputX, null, null);
            stream.skipRaw();

            String inputY = stream.readInput();
            Double y = doubleParser.resolve(context, stream, inputY);
            if(y == null ) throw new InvalidLocationFormatException(input, InvalidLocationFormatException.Reason.INVALID_Y_COORD, null, inputY, null);
            stream.skipRaw();

            String inputZ = stream.readInput();
            Double z = doubleParser.resolve(context, stream, inputZ);
            if(z == null) throw new InvalidLocationFormatException(input, InvalidLocationFormatException.Reason.INVALID_Z_COORD, null, null, inputZ);

            return new Location(world, x, y, z);
        }

    }

    private @NotNull Location locFromStr(ExecutionContext<BukkitSource> context, CommandInputStream<BukkitSource> stream, String currentRaw) throws ImperatException {
        String[] split = currentRaw.split(SINGLE_STRING_SEPARATOR);
        if(split.length < 4) {
            throw new InvalidLocationFormatException(currentRaw, InvalidLocationFormatException.Reason.WRONG_FORMAT);
        }

        World world = Bukkit.getWorld(split[0]);
        if(world == null) {
            throw new UnknownWorldException(split[0]);
        }

        Double x = Objects.requireNonNull(doubleParser.resolve(context, stream, split[1]));
        Double y = Objects.requireNonNull(doubleParser.resolve(context, stream, split[2]));
        Double z = Objects.requireNonNull(doubleParser.resolve(context, stream, split[3]));

        return new Location(world, x, y, z);
    }

}
