package dev.velix.imperat.annotations.injectors;

import dev.velix.imperat.CommandDispatcher;
import dev.velix.imperat.annotations.CommandAnnotationRegistry;
import dev.velix.imperat.annotations.MethodCommandExecutor;
import dev.velix.imperat.annotations.element.CommandAnnotatedElement;
import dev.velix.imperat.annotations.element.ParameterCommandElement;
import dev.velix.imperat.annotations.parameters.AnnotatedParameter;
import dev.velix.imperat.annotations.types.Description;
import dev.velix.imperat.annotations.types.Permission;
import dev.velix.imperat.annotations.types.methods.*;
import dev.velix.imperat.annotations.types.parameters.*;
import dev.velix.imperat.annotations.types.parameters.Optional;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.cooldown.UsageCooldown;
import dev.velix.imperat.command.parameters.UsageParameter;
import dev.velix.imperat.help.MethodHelpExecution;
import dev.velix.imperat.resolvers.OptionalValueSupplier;
import dev.velix.imperat.util.Registry;
import dev.velix.imperat.util.annotations.MethodVerifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public final class AnnotationDataRegistry<C> extends
				Registry<Class<? extends Annotation>, AnnotationDataInjector<?, C, ?>> {
	
	
	private final Map<Class<? extends Annotation>, AnnotationDataCreator<?, ?>> dataCreators = new HashMap<>();
	
	public AnnotationDataRegistry(CommandAnnotationRegistry annotationRegistry, CommandDispatcher<C> dispatcher) {
		super(LinkedHashMap::new);
		registerDataCreator(
						dev.velix.imperat.annotations.types.Command.class,
						(proxy, commandAnnotation, element) -> {
							final String[] values = commandAnnotation.value();
							List<String> aliases = new ArrayList<>(Arrays.asList(values)
											.subList(1, values.length));
							Command<C> cmd = Command.createCommand(values[0]);
							cmd.ignoreACPermissions(commandAnnotation.ignoreAutoCompletionPermission());
							cmd.addAliases(aliases);
							return cmd;
						}
		);
		registerDataCreator(Usage.class, (proxy, annotation, element) -> {
			Method method = (Method) element.getElement();
			var params = loadParameters(annotationRegistry, dispatcher, null, null, method);
			return CommandUsage.<C>builder()
							.parameters(params);
		});
		
		registerCommandInjector(DefaultUsage.class, (proxy, command, toLoad, element, annotation) -> {
			Method method = (Method) element.getElement();
			if (element.isAnnotationPresent(Usage.class) || element.isAnnotationPresent(SubCommand.class)) {
				throw new IllegalArgumentException("A default usage in method '" + method.getName() + "' in class '" + proxy.getName() + "'");
			}
			
			command.setDefaultUsageExecution(new MethodCommandExecutor<>(proxy, dispatcher, method,
							Collections.emptyList()));
		});
		registerSubCmdInjector(annotationRegistry, dispatcher);
		
		registerCommandInjector(Help.class, ((proxy, command, toLoad, element, annotation) -> {
			MethodVerifier.verifyHelpMethod(dispatcher, toLoad.getMainUsage(), proxy,
							(Method) element.getElement());
			command.addHelpCommand(dispatcher,
							new MethodHelpExecution<>(proxy, (Method) element.getElement()));
		}));
		
		//TODO register for @DefaultValue @DefaultValueProvider
		
		registerUsageInjector(Cooldown.class, (proxy, command, toLoad, element, annotation) -> toLoad.cooldown(loadCooldown(element)));
		registerInjector(Permission.class, (proxy, command, toLoad, element, annotation) -> {
			command.setPermission(annotation.value());
			if (toLoad instanceof CommandUsage.Builder<?> builder) {
				builder.permission(annotation.value());
			} else {
				command.setPermission(annotation.value());
			}
		});
		registerInjector(Description.class, ((proxy, command, toLoad, element, annotation) -> {
			command.setDescription(annotation.value());
			if (toLoad instanceof CommandUsage<?> usage) {
				usage.setDescription(annotation.value());
			} else {
				command.setDescription(annotation.value());
			}
		}));
	}
	
	private void registerSubCmdInjector(CommandAnnotationRegistry annotationRegistry, CommandDispatcher<C> dispatcher) {
		registerCommandInjector(SubCommand.class, (proxy, command, toLoad, element, annotation) -> {
			Method method = (Method) element.getElement();
			
			if (element.isAnnotationPresent(Usage.class)) {
				logError(method, proxy, "@Usage and @SubCommand cannot be used together (only one of them is allowed per method)");
				return;
			}
			if (element.isAnnotationPresent(Help.class)) {
				logError(method, proxy, "@SubCommand cannot be treated as a help , @Help was used with @SubCommand");
				return;
			}
			
			final String[] values = annotation.value();
			List<String> aliases = new ArrayList<>(Arrays.asList(values)
							.subList(1, values.length));
			
			var mainUsage = command.getMainUsage();
			List<UsageParameter> methodUsageParameters = this.loadParameters(annotationRegistry, dispatcher, annotation, mainUsage, method);
			
			UsageCooldown cooldown = loadCooldown(element);
			String desc = element.isAnnotationPresent(Description.class) ? element.getAnnotation(Description.class).value() : "N/A";
			String permission = element.isAnnotationPresent(Permission.class) ? element.getAnnotation(Permission.class).value() : null;
			toLoad.addSubCommandUsage(values[0], aliases,
							CommandUsage.<C>builder()
											.parameters(methodUsageParameters)
											.description(desc)
											.permission(permission)
											.cooldown(cooldown)
											.execute(new MethodCommandExecutor<>(proxy, dispatcher, method,
															methodUsageParameters)).build(),
							annotation.attachDirectly()
			);
		});
	}
	
	private UsageCooldown loadCooldown(CommandAnnotatedElement<?> element) {
		if (!element.isAnnotationPresent(Cooldown.class)) return null;
		var cooldownAnnotation = element.getAnnotation(Cooldown.class);
		return new UsageCooldown(cooldownAnnotation.value(), cooldownAnnotation.unit());
	}
	
	public <A extends Annotation> void registerDataCreator(Class<A> annotationType, AnnotationDataCreator<?, A> creator) {
		dataCreators.put(annotationType, creator);
	}
	
	@SuppressWarnings("unchecked")
	public @Nullable <A extends Annotation, O> AnnotationDataCreator<O, A> getDataCreator(Class<A> annotationClass) {
		return (AnnotationDataCreator<O, A>) dataCreators.get(annotationClass);
	}
	
	public <A extends Annotation, O> void registerInjector(
					Class<A> aClass,
					AnnotationDataInjector<O, C, A> injector
	) {
		setData(aClass, injector);
	}
	
	public <A extends Annotation> void registerUsageInjector(Class<A> aClass, AnnotationDataInjector<CommandUsage.Builder<C>, C, A> injector) {
		this.registerInjector(aClass, injector);
	}
	
	public <A extends Annotation> void registerCommandInjector(Class<A> aClass, AnnotationDataInjector<Command<C>, C, A> injector) {
		this.registerInjector(aClass, injector);
	}
	
	
	@SuppressWarnings("unchecked")
	public @Nullable <A extends Annotation> AnnotationDataInjector<?, C, A> getInjector(Class<A> clazz) {
		return (AnnotationDataInjector<?, C, A>) getData(clazz).orElse(null);
	}
	
	@SuppressWarnings("unchecked")
	public <A extends Annotation, O> void injectForElement(
					@NotNull Class<?> proxy,
					@NotNull Command<C> command,
					@NotNull O toLoad,
					@NotNull CommandAnnotatedElement<?> element
	) {
		
		for (Annotation annotation : element) {
			AnnotationDataInjector<O, C, A> injector =
							(AnnotationDataInjector<O, C, A>) getInjector(annotation.annotationType());
			if (injector == null) continue;
			injector.inject(proxy, command, toLoad,
							element, (A) annotation);
		}
		
	}
	
	private List<UsageParameter> loadParameters(CommandAnnotationRegistry registry,
	                                            CommandDispatcher<C> dispatcher,
	                                            @Nullable SubCommand subCommand,
	                                            @Nullable CommandUsage<C> mainUsage,
	                                            Method method) {
		
		List<UsageParameter> usageParameters = new ArrayList<>();
		for (Parameter parameter : method.getParameters()) {
			if (dispatcher.canBeSender(parameter.getType())) continue;
			
			UsageParameter usageParameter = getParameter(registry, parameter);
			if (usageParameter != null) {
				if (dispatcher.hasContextResolver(parameter.getType())) continue;
				if (subCommand != null && mainUsage != null && mainUsage
								.hasParameter((param) -> param.equals(usageParameter))) {
					continue;
				}
				usageParameters.add(usageParameter);
			}
		}
		return usageParameters;
	}
	
	
	@SuppressWarnings("unchecked")
	private <T> UsageParameter getParameter(
					CommandAnnotationRegistry registry,
					Parameter parameter
	) {
		Named named = parameter.getAnnotation(Named.class);
		Flag flag = parameter.getAnnotation(Flag.class);
		
		String name;
		boolean optional = parameter.getAnnotation(Optional.class) != null;
		
		if (named != null) {
			name = named.value();
		} else if (flag != null) {
			name = flag.value();
			optional = true;
		} else {
			name = parameter.getName();
		}
		
		boolean greedy = parameter.getAnnotation(Greedy.class) != null;
		
		if (greedy && parameter.getType() != String.class) {
			throw new IllegalArgumentException("Argument '" + parameter.getName() + "' is greedy while having a non-greedy type '" + parameter.getType().getName() + "'");
		}
		
		UsageParameter usageParameter;
		
		if (flag != null) {
			usageParameter = AnnotatedParameter.flag(name, null);
		} else {
			
			OptionalValueSupplier<C, T> optionalValueSupplier = null;
			if (optional) {
				DefaultValue defaultValueAnnotation = parameter.getAnnotation(DefaultValue.class);
				DefaultValueProvider provider = parameter.getAnnotation(DefaultValueProvider.class);
				
				
				if (defaultValueAnnotation != null) {
					String def = defaultValueAnnotation.value();
					optionalValueSupplier = (OptionalValueSupplier<C, T>) OptionalValueSupplier.of(def);
				} else if (provider != null) {
					Class<? extends OptionalValueSupplier<?, ?>> supplierClass = provider.value();
					try {
						optionalValueSupplier = getOptionalValueSupplier(parameter, supplierClass);
					} catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
					         IllegalAccessException e) {
						throw new RuntimeException("Optional value suppler class '" + supplierClass.getName() + "' doesn't have an empty accessible constructor !");
					}
				}
			}
			
			usageParameter = AnnotatedParameter.input(name, parameter.getType(),
							optional, greedy, new ParameterCommandElement(registry, name, parameter), optionalValueSupplier);
		}
		
		
		return usageParameter;
	}
	
	@SuppressWarnings({"unchecked"})
	private static <C, T> @NotNull OptionalValueSupplier<C, T> getOptionalValueSupplier(Parameter parameter, Class<? extends OptionalValueSupplier<?, ?>> supplierClass) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		var emptyConstructor = supplierClass.getDeclaredConstructor();
		if (!emptyConstructor.isAccessible()) {
			emptyConstructor.setAccessible(true);
		}
		OptionalValueSupplier<C, T> valueSupplier = (OptionalValueSupplier<C, T>) emptyConstructor.newInstance();
		if (valueSupplier.getValueType() != parameter.getType()) {
			throw new IllegalArgumentException("Optional supplier of value-type '" + valueSupplier.getValueType().getName() + "' doesn't match the optional arg type '" + parameter.getType().getName() + "'");
		}
		return valueSupplier;
	}
	
	private static void logError(Method method, Class<?> proxy, String msg) {
		throw new IllegalArgumentException(String.format(msg + " in method '%s' of class '%s'", method.getName(), proxy.getName()));
	}
	
}


