package dev.velix.imperat.selector;

import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.SourceException;
import dev.velix.imperat.util.TypeUtility;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;

public enum TargetField {


    NAME {
        @Override
        public boolean handle(
            CommandSender sender,
            Entity entity,
            List<Entity> collectedEntities,
            String value,
            CommandInputStream<?> inputStream
        ) throws ImperatException {
            return entity.getName().equalsIgnoreCase(value);
        }
    },

    DISTANCE {
        private final static String RANGE = "..";

        @Override
        public boolean handle(
            CommandSender sender,
            Entity entity,
            List<Entity> collectedEntities,
            String value,
            CommandInputStream<?> inputStream
        ) throws ImperatException {
            if (!(sender instanceof Player player)) {
                throw new SourceException("Only players can use the field=`distance`");
            }

            double diff = entity.getLocation().distance(player.getLocation());
            String inputValue = value.replace(RANGE, "");
            if (!TypeUtility.isNumber(inputValue)) {
                throw new SourceException("Invalid distance value '%s'", value);
            }

            double rangeValue = Double.parseDouble(inputValue);
            if (value.startsWith(RANGE)) {
                //less than or equal to value
                return diff <= rangeValue;
            } else if (value.endsWith(RANGE)) {
                return diff >= rangeValue;
            } else if (value.contains(RANGE)) {
                String[] minMaxSplit = value.split(RANGE);
                if (minMaxSplit.length > 2) {
                    throw new SourceException("Invalid distance range format '%s'", value);
                }
                String minStr = minMaxSplit[0], maxStr = minMaxSplit[1];
                if (!TypeUtility.isNumber(minStr)) {
                    throw new SourceException("Invalid distance min-value '%s'", minStr);
                }

                if (!TypeUtility.isNumber(maxStr)) {
                    throw new SourceException("Invalid distance max-value '%s'", maxStr);
                }

                double min = Double.parseDouble(minStr), max = Double.parseDouble(maxStr);
                return diff >= min && diff <= max;
            }

            return rangeValue == diff;
        }
    },

    TYPE {
        @Override
        public boolean handle(
            CommandSender sender,
            Entity entity,
            List<Entity> collectedEntities,
            String value,
            CommandInputStream<?> inputStream
        ) throws ImperatException {
            try {
                EntityType type = EntityType.valueOf(value);
                return entity.getType() == type;
            } catch (EnumConstantNotPresentException ex) {
                throw new SourceException("Unknown type '%s'", value);
            }
        }
    },

    LIMIT {
        @Override
        public boolean handle(
            CommandSender sender,
            Entity entity,
            List<Entity> collectedEntities,
            String value,
            CommandInputStream<?> inputStream
        ) throws ImperatException {
            inputStream.skipTill('=');
            if (!TypeUtility.isInteger(value)) {
                throw new SourceException("Invalid limit-value integer '%s'", value);
            }
            int limit = Integer.parseInt(value);
            return collectedEntities.size() <= limit;
        }
    },

    COORDINATES {
        @Override
        public boolean handle(
            CommandSender sender,
            Entity entity,
            List<Entity> collectedEntities,
            String value,
            CommandInputStream<?> inputStream
        ) throws ImperatException {


            //TODO handle this somehow
            int x1 = -1, y1 = -1, z1 = -1;


            return false;
        }
    },


    TAG {
        @Override
        public boolean handle(
            CommandSender sender,
            Entity entity,
            List<Entity> collectedEntities,
            String value,
            CommandInputStream<?> inputStream
        ) throws ImperatException {
            return entity.hasMetadata(value);
        }
    },

    SORT {
        @Override
        public boolean handle(
            CommandSender sender,
            Entity entity,
            List<Entity> collectedEntities,
            String value,
            CommandInputStream<?> inputStream
        ) throws ImperatException {
            //TODO this tag is special, needs a different approach internally from the ParameterTargetSelector or we can do this untested approach below:
            //TODO for the approach above, create a comparator method for each type of sorting.
            collectedEntities.sort(null);
            return true;
        }
    },

    SCORES {
        @Override
        public boolean handle(
            CommandSender sender,
            Entity entity,
            List<Entity> collectedEntities,
            String value,
            CommandInputStream<?> inputStream
        ) throws ImperatException {
            return false;
        }
    },

    GAMEMODE {
        @Override
        public boolean handle(
            CommandSender sender,
            Entity entity,
            List<Entity> collectedEntities,
            String value,
            CommandInputStream<?> inputStream
        ) throws ImperatException {
            if (!(entity instanceof Player player)) {
                return false;
            }
            try {
                GameMode mode = GameMode.valueOf(value);
                return player.getGameMode() == mode;
            } catch (EnumConstantNotPresentException ex) {
                throw new SourceException("Unknown gamemode '%s'", value);
            }
        }
    },

    LEVEL {
        @Override
        public boolean handle(
            CommandSender sender,
            Entity entity,
            List<Entity> collectedEntities,
            String value,
            CommandInputStream<?> inputStream
        ) throws ImperatException {
            if (!(entity instanceof Player player)) {
                return false;
            }
            if (!TypeUtility.isInteger(value)) {
                throw new SourceException("Invalid level value format '%s'", value);
            }
            return player.getLevel() == Integer.parseInt(value);
        }
    },

    X_ROTATION {
        @Override
        public boolean handle(
            CommandSender sender,
            Entity entity,
            List<Entity> collectedEntities,
            String value,
            CommandInputStream<?> inputStream
        ) throws ImperatException {
            return false;
        }
    },

    Y_ROTATION {
        @Override
        public boolean handle(
            CommandSender sender,
            Entity entity,
            List<Entity> collectedEntities,
            String value,
            CommandInputStream<?> inputStream
        ) throws ImperatException {
            return false;
        }
    },

    NBT {
        @Override
        public boolean handle(
            CommandSender sender,
            Entity entity,
            List<Entity> collectedEntities,
            String value,
            CommandInputStream<?> inputStream
        ) throws ImperatException {
            return false;
        }
    };

    final static char VALUE_EQUALS = '=', SEPARATOR = ',';

    public boolean matchesInput(String fieldInput) {
        return this.name().equalsIgnoreCase(fieldInput);
    }

    public abstract boolean handle(CommandSender sender, Entity entity, List<Entity> collectedEntities, String value, CommandInputStream<?> inputStream) throws ImperatException;

}
