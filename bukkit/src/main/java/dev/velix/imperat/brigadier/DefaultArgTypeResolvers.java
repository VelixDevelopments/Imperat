package dev.velix.imperat.brigadier;

import com.mojang.brigadier.arguments.*;
import dev.velix.imperat.ArgumentTypeResolver;
import dev.velix.imperat.BukkitCommandDispatcher;
import dev.velix.imperat.command.parameters.NumericRange;
import dev.velix.imperat.util.TypeUtility;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

class DefaultArgTypeResolvers {

	public final static ArgumentTypeResolver STRING = (parameter -> {
		if(parameter.isGreedy())return StringArgumentType.greedyString();
		return StringArgumentType.string();
	});
	
	public final static ArgumentTypeResolver BOOLEAN = (parameter -> BoolArgumentType.bool());
	
	public final static ArgumentTypeResolver NUMERIC = (parameter)-> {
		
		Class<?> type = parameter.getType();
		if(parameter.isNumeric()) {
			NumericRange range = parameter.asNumeric().getRange();
			return numeric(type, range);
		}
		
		return null;
	};
	
	private static final ArgumentType<?> SINGLE_PLAYER = entity(true, true);
	private static final ArgumentType<?> MULTI_PLAYER = entity(false, true);
	private static final ArgumentType<?> MULTI_ENTITY = entity(false, false);
	
	public static final ArgumentTypeResolver PLAYER = parameter -> SINGLE_PLAYER;
	
	public static final ArgumentTypeResolver ENTITY_SELECTOR = parameter -> {
		Class<? extends Entity> type = BukkitCommandDispatcher.getSelectedEntity(parameter.getGenericType());
		if (Player.class.isAssignableFrom(type)) // EntitySelector<Player>
			return MULTI_PLAYER;
		return MULTI_ENTITY;
	};
	
	private static ArgumentType<? extends Number> numeric(
					Class<?> type,
					@Nullable NumericRange range) {
		if(TypeUtility.matches(type, int.class)) {
			return IntegerArgumentType.integer((int) getMin(range), (int) getMax(range));
		} else if (TypeUtility.matches(type, long.class)) {
			return LongArgumentType.longArg((long) getMin(range), (long) getMax(range));
		} else if (TypeUtility.matches(type, float.class)) {
			return FloatArgumentType.floatArg((float) getMin(range), (float) getMax(range));
		} else if (TypeUtility.matches(type, double.class)) {
			return DoubleArgumentType.doubleArg(getMin(range), getMax(range));
		} else {
			throw new IllegalArgumentException("Unsupported numeric type: " + type);
		}
	}
	
	private static double getMin(@Nullable NumericRange range) {
		if(range == null)
			return Double.MIN_VALUE;
		else
			return range.getMin();
	}
	
	private static double getMax(@Nullable NumericRange range) {
		if(range == null)
			return Double.MAX_VALUE;
		else
			return range.getMax();
	}
	
	private static ArgumentType<?> entity(boolean single, boolean playerOnly) {
		return MinecraftArgumentType.ENTITY.create(single, playerOnly);
	}
}