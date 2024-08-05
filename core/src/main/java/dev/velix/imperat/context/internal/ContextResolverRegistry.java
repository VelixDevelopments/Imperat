package dev.velix.imperat.context.internal;

import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.EnumContextResolver;
import dev.velix.imperat.exceptions.context.ContextResolveException;
import dev.velix.imperat.util.TypeUtility;
import dev.velix.imperat.resolvers.ContextResolver;
import dev.velix.imperat.util.Registry;
import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.TimeUnit;

@ApiStatus.Internal
public final class ContextResolverRegistry<C> extends Registry<Class<?>, ContextResolver<C, ?>> {

	public ContextResolverRegistry() {
		super();
		registerResolver(String.class, ((source, context, raw) -> raw));
		registerResolver(Integer.class, (source, context, raw) -> {
			if(TypeUtility.isInteger(raw)) {
				return Integer.parseInt(raw);
			}else {
				throw exception(context, raw, Integer.class);
			}
		});
		registerResolver(Long.class, (source, context, raw)-> {
			if(TypeUtility.isLong(raw)) {
				return Long.parseLong(raw);
			}else {
				throw exception(context, raw, Long.class);
			}
		});
		registerResolver(Boolean.class, (source, context, raw) -> {
			if(TypeUtility.isBoolean(raw)) {
				return Boolean.valueOf(raw);
			}else {
				throw exception(context, raw, Boolean.class);
			}
		});
		registerResolver(Double.class, (source, context, raw)-> {
			if(TypeUtility.isDouble(raw)) {
				return Double.parseDouble(raw);
			}else {
				throw exception(context, raw, Double.class);
			}
		});
		registerResolver(TimeUnit.class, new EnumContextResolver<>(TimeUnit.class));
	}

	private ContextResolveException exception(Context<C> context,
	                                          String raw,
	                                          Class<?> clazzRequired) {
		return new ContextResolveException("Error while parsing " + context.getCommandUsed()
				  + "'s argument input '" + raw + "' , it's NOT any type of " + clazzRequired.getSimpleName());
	}

	public <T> void registerResolver(Class<T> clazz, ContextResolver<C, T> resolver) {
		setData(clazz, resolver);
	}

	public <E extends Enum<E>> void registerEnumResolver(Class<E> enumClass) {
		registerResolver(enumClass, new EnumContextResolver<>(enumClass));
	}

	@SuppressWarnings("unchecked")
	public <T> ContextResolver<C, T> getResolver(Class<T> type) {
		return (ContextResolver<C, T>) getData(type).orElse(null);
	}

}
