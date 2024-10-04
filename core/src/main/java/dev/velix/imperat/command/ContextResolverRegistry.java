package dev.velix.imperat.command;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.base.element.ParameterElement;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.help.CommandHelp;
import dev.velix.imperat.resolvers.ContextResolver;
import dev.velix.imperat.util.Registry;
import dev.velix.imperat.util.TypeUtility;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Optional;

@ApiStatus.AvailableSince("1.0.0")
final class ContextResolverRegistry<S extends Source> extends Registry<Type, ContextResolver<S, ?>> implements ContextResolverFactory<S> {
	
	private final Registry<Type, ContextResolverFactory<S>> factories = new Registry<>();
	
	private ContextResolverRegistry(final Imperat<S> imperat) {
		super();
		this.registerResolver(TypeWrap.of(CommandHelp.class).getType(), (ctx, param) -> new CommandHelp(imperat, ctx));
	}
	
	public static <S extends Source> ContextResolverRegistry<S> createDefault(final Imperat<S> imperat) {
		return new ContextResolverRegistry<>(imperat);
	}
	
	public <T> void registerResolver(Type type, ContextResolver<S, T> resolver) {
		setData(type, resolver);
	}
	
	public void registerFactory(Type type, ContextResolverFactory<S> factory) {
		factories.setData(type, factory);
	}
	
	public Optional<ContextResolverFactory<S>> getFactoryFor(Type type) {
		return factories.getData(type);
	}
	
	@SuppressWarnings("unchecked")
	<T> @Nullable ContextResolver<S, T> getContextResolver(Type type, @Nullable ParameterElement element) {
		//we search for factories mainly
		return (ContextResolver<S, T>) getFactoryFor(type)
			.flatMap((factory) -> Optional.ofNullable(factory.create(type, element)))
			.orElseGet(() -> this.create(type, element));
	}
	
	public <T> @Nullable ContextResolver<S, T> getResolverWithoutParameterElement(Type type) {
		//
		ContextResolver<S, T> resultFromFactory = getContextResolver(type, null);
		if (resultFromFactory == null) {
			for (var registeredType : getKeys()) {
				if (TypeUtility.areRelatedTypes(type, registeredType)) {
					return getResolverWithoutParameterElement(registeredType);
				}
			}
			return null;
		}
		return resultFromFactory;
	}
	
	/**
	 * Creates a context resolver based on the parameter
	 *
	 * @param parameter the parameter (null if used classic way)
	 * @return the {@link ContextResolver} specific for that parameter
	 */
	@Override
	@SuppressWarnings("unchecked")
	public @Nullable <T> ContextResolver<S, T> create(Type type, @Nullable ParameterElement parameter) {
		return (ContextResolver<S, T>) getData(type).orElse(null);
	}
}
