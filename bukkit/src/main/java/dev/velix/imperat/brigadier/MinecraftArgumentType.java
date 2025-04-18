package dev.velix.imperat.brigadier;

import com.mojang.brigadier.arguments.ArgumentType;
import dev.velix.imperat.util.BukkitUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings({"all"})
public enum MinecraftArgumentType {

    /**
     * A selector, player name, or UUID.
     * <p>
     * ParameterBuilder:
     * - boolean single
     * - boolean playerOnly
     */
    ENTITY("ArgumentEntity", boolean.class, boolean.class),

    /**
     * A player, online or not. Can also use a selector, which may match one or more
     * players (but not entities).
     */
    GAME_PROFILE("ArgumentProfile"),

    /**
     * A chat color. One of the names from <a href="https://wiki.vg/Chat#Colors">colors</a>, or {@code reset}.
     * Case-insensitive.
     */
    COLOR("ArgumentChatFormat"),

    /**
     * A JSON Chat component.
     */
    COMPONENT("ArgumentChatComponent"),

    /**
     * A regular message, potentially including selectors.
     */
    MESSAGE("ArgumentChat"),

    /**
     * An NBT value, parsed using JSON-NBT rules. This represents a full NBT tag.
     */
    NBT("ArgumentNBTTag"),

    /**
     * Represents a partial NBT tag, usable in data modify command.
     */
    NBT_TAG("ArgumentNBTBase"),

    /**
     * A path within an NBT value, allowing for array and member accesses.
     */
    NBT_PATH("ArgumentNBTKey"),

    /**
     * A scoreboard objective.
     */
    SCOREBOARD_OBJECTIVE("ArgumentScoreboardObjective"),

    /**
     * A single score criterion.
     */
    OBJECTIVE_CRITERIA("ArgumentScoreboardCriteria"),

    /**
     * A scoreboard operator.
     */
    SCOREBOARD_SLOT("ArgumentScoreboardSlot"),

    /**
     * Something that can join a team. Allows selectors and *.
     */
    SCORE_HOLDER("ArgumentScoreholder"),

    /**
     * The name of a team. Parsed as an unquoted string.
     */
    TEAM("ArgumentScoreboardTeam"),

    /**
     * A scoreboard operator.
     */
    OPERATION("ArgumentMathOperation"),

    /**
     * A particle effect (an identifier with extra information following it for
     * specific particles, mirroring the Particle packet)
     */
    PARTICLE("ArgumentParticle"),

    /**
     * Represents an angle.
     */
    ANGLE("ArgumentAngle"),

    /**
     * A name for an inventory slot.
     */
    ITEM_SLOT("ArgumentInventorySlot"),

    /**
     * An Identifier.
     */
    RESOURCE_LOCATION("ArgumentMinecraftKeyRegistered"),

    /**
     * A potion effect.
     */
    POTION_EFFECT("ArgumentMobEffect"),

    /**
     * Represents a item enchantment.
     */
    ENCHANTMENT("ArgumentEnchantment"),

    /**
     * Represents an entity summon.
     */
    ENTITY_SUMMON("ArgumentEntitySummon"),

    /**
     * Represents a dimension.
     */
    DIMENSION("ArgumentDimension"),

    /**
     * Represents a time duration.
     */
    TIME("ArgumentTime"),

    /**
     * Represents a UUID value.
     *
     * @since Minecraft 1.16
     */
    UUID("ArgumentUUID"),

    /**
     * A location, represented as 3 numbers (which must be integers). May use relative locations
     * with ~
     */
    BLOCK_POS("coordinates.ArgumentPosition"),

    /**
     * A column location, represented as 3 numbers (which must be integers). May use relative locations
     * with ~.
     */
    COLUMN_POS("coordinates.ArgumentVec2I"),

    /**
     * A location, represented as 3 numbers (which may have a decimal point, but will be moved to the
     * center of a block if none is specified). May use relative locations with ~.
     */
    VECTOR_3("coordinates.ArgumentVec3"),

    /**
     * A location, represented as 2 numbers (which may have a decimal point, but will be moved to the center
     * of a block if none is specified). May use relative locations with ~.
     */
    VECTOR_2("coordinates.ArgumentVec2"),

    /**
     * An angle, represented as 2 numbers (which may have a decimal point, but will be moved to the
     * center of a block if none is specified). May use relative locations with ~.
     */
    ROTATION("coordinates.ArgumentRotation"),

    /**
     * A collection of up to 3 axes.
     */
    SWIZZLE("coordinates.ArgumentRotationAxis"),

    /**
     * A block state, optionally including NBT and state information.
     */
    BLOCK_STATE("blocks.ArgumentTile"),

    /**
     * A block, or a block tag.
     */
    BLOCK_PREDICATE("blocks.ArgumentBlockPredicate"),

    /**
     * An item, optionally including NBT.
     */
    ITEM_STACK("item.ArgumentItemStack"),

    /**
     * An item, or an item tag.
     */
    ITEM_PREDICATE("item.ArgumentItemPredicate"),

    /**
     * A function.
     */
    FUNCTION("item.ArgumentTag"),

    /**
     * The entity anchor related to the facing argument in the teleport command,
     * is feet or eyes.
     */
    ENTITY_ANCHOR("ArgumentAnchor"),

    /**
     * An integer range of values with a min and a max.
     */
    INT_RANGE("ArgumentCriterionValue$b"),

    /**
     * A floating-point range of values with a min and a max.
     */
    FLOAT_RANGE("ArgumentCriterionValue$a"),

    /**
     * Template mirror
     *
     * @since Minecraft 1.19
     */
    TEMPLATE_MIRROR("TemplateMirrorArgument"),

    /**
     * Template rotation
     *
     * @since Minecraft 1.19
     */
    TEMPLATE_ROTATION("TemplateRotationArgument");

    private final Class<?>[] parameters;
    private @Nullable ArgumentType<?> argumentType;
    private @Nullable Constructor<? extends ArgumentType> argumentConstructor;

    MinecraftArgumentType(String name, Class<?>... parameters) {
        Class<?> argumentClass = resolveArgumentClass(name);
        this.parameters = parameters;
        if (argumentClass == null) {
            argumentType = null;
            argumentConstructor = null;
            return;
        }
        try {
            argumentConstructor = argumentClass.asSubclass(ArgumentType.class).getDeclaredConstructor(parameters);
            if (!argumentConstructor.isAccessible())
                argumentConstructor.setAccessible(true);
            if (parameters.length == 0) {
                argumentType = argumentConstructor.newInstance();
            } else {
                argumentType = null;
            }
        } catch (Throwable e) {
            argumentType = null;
            argumentConstructor = null;
        }
    }

    private static @Nullable Class<?> resolveArgumentClass(String name) {
        try {
            if (BukkitUtil.ClassesRefUtil.minecraftVersion() > 16) {
                return BukkitUtil.ClassesRefUtil.mcClass("commands.arguments." + name);
            } else {
                String stripped;
                if (name.lastIndexOf('.') != -1)
                    stripped = name.substring(name.lastIndexOf('.') + 1);
                else
                    stripped = name;
                return BukkitUtil.ClassesRefUtil.nmsClass(stripped);
            }
        } catch (Throwable t) {
            return null;
        }
    }

    public static void ensureSetup() {
        // do nothing - this is only called to trigger the static initializer
    }

    /**
     * Checks if this argument valueType is supported in this Minecraft version
     *
     * @return If this is supported
     */
    public boolean isSupported() {
        return argumentConstructor != null;
    }

    /**
     * Checks if this argument valueType requires parameters
     *
     * @return If this requires parameters
     */
    public boolean requiresParameters() {
        return parameters.length != 0;
    }

    /**
     * Returns the argument valueType represented by this enum value, otherwise
     * throws an exception
     *
     * @param <T> The argument valueType
     * @return The argument valueType
     * @throws IllegalArgumentException if not supported in this version
     * @throws IllegalArgumentException if this argument requires arguments. See {@link #create(Object...)}
     */
    public @NotNull <T> ArgumentType<T> get() {
        if (argumentConstructor == null)
            throw new IllegalArgumentException("Argument valueType '" + name().toLowerCase() + "' is not available on this version.");
        if (argumentType != null)
            return (ArgumentType<T>) argumentType;
        throw new IllegalArgumentException("This argument valueType requires " + parameters.length + " parameter(s) of valueType(s) " +
            Arrays.stream(parameters).map(Class::getName).collect(Collectors.joining(", ")) + ". Use #create() instead.");
    }

    /**
     * Creates an instance of this argument valueType
     *
     * @param arguments Arguments to construct the argument valueType with
     * @param <T>       The argument ttype
     * @return The created argument valueType.
     * @throws IllegalArgumentException if not supported in this version
     */
    public @NotNull <T> ArgumentType<T> create(Object... arguments) {
        if (argumentConstructor == null) {
            throw new IllegalArgumentException("Argument valueType '" + name().toLowerCase() + "' is not available on this version.");
        }

        if (argumentType != null && arguments.length == 0) {
            return (ArgumentType<T>) argumentType;
        }

        try {
            return argumentConstructor.newInstance(arguments);
        } catch (final Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the argument valueType represented by this enum value, wrapped
     * inside an {@link Optional}
     *
     * @param <T> The argument valueType
     * @return The argument valueType optional
     * @throws IllegalArgumentException if this argument requires arguments. See {@link #createIfPresent(Object...)}
     */
    public @NotNull <T> Optional<ArgumentType<T>> getIfPresent() {
        if (argumentConstructor == null)
            return Optional.empty();
        if (argumentType != null)
            return Optional.of((ArgumentType<T>) argumentType);
        throw new IllegalArgumentException("This argument valueType requires " + parameters.length + " parameter(s) of valueType(s) " +
            Arrays.stream(parameters).map(Class::getName).collect(Collectors.joining(", ")) + ". Use #create() instead.");
    }

    /**
     * Creates an instance of this argument valueType, wrapped in an optional.
     *
     * @param arguments Arguments to construct the argument valueType with
     * @param <T>       The argument ttype
     * @return The created argument valueType optional.
     */
    public @NotNull <T> Optional<ArgumentType<T>> createIfPresent(Object... arguments) {
        if (argumentConstructor == null) {
            return Optional.empty();
        }

        if (argumentType != null && arguments.length == 0) {
            return Optional.of((ArgumentType<T>) argumentType);
        }

        try {
            return Optional.of(argumentConstructor.newInstance(arguments));
        } catch (final Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
