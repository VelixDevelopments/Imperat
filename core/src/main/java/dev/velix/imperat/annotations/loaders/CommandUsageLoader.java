package dev.velix.imperat.annotations.loaders;

import dev.velix.imperat.CommandDispatcher;
import dev.velix.imperat.annotations.AnnotationLoader;
import dev.velix.imperat.annotations.MethodCommandExecutor;
import dev.velix.imperat.annotations.parameters.AnnotatedParameter;
import dev.velix.imperat.annotations.types.Permission;
import dev.velix.imperat.annotations.types.methods.*;
import dev.velix.imperat.annotations.types.parameters.*;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.UsageParameter;
import dev.velix.imperat.help.CommandHelp;
import dev.velix.imperat.util.MethodVerifier;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CommandUsageLoader<C> implements AnnotationLoader<C, CommandUsage<C>> {

	private final CommandDispatcher<C> dispatcher;
	private final Class<?> aClass;
	private final Object instance;
	private final Method method;

	public CommandUsageLoader(CommandDispatcher<C> dispatcher,
	                          Object instance,
	                          Class<?> clazz,
	                          Method method) {
		this.dispatcher = dispatcher;
		this.method = method;
		this.aClass = clazz;
		this.instance = instance;
	}

	/**
	 * Loads the object using the annotation
	 *
	 * @param alreadyLoaded the already loaded object
	 * @return the object loaded from the annotation
	 */
	@Override
	public @Nullable CommandUsage<C> load(@Nullable Command<C> alreadyLoaded) {
		if (alreadyLoaded == null) {
			return null;
		}


		Usage usage = method.getAnnotation(Usage.class);
		SubCommand subCommand = method.getAnnotation(SubCommand.class);

		if (method.getAnnotation(DefaultUsage.class) != null) {
			MethodVerifier.verifyMethod(dispatcher, aClass, method, true);
			alreadyLoaded.setDefaultUsageExecution(
					  new MethodCommandExecutor<>(instance, dispatcher, alreadyLoaded,
							    method, Collections.emptyList(), null)
			);
			return null;
		}


		if (usage == null && subCommand == null) {
			//System.out.println("NO USAGE ANNOTATION !");
			throw new IllegalStateException("Couldn't find @Usage or @SubCommand in method '" + method.getName() + "' in class '" + aClass.getName() + "'");
		} else if (usage != null && subCommand != null) {
			throw new IllegalStateException("Found annotation duplicates of similar context '@Usage' and '@SubCommand' in the same method !");
		}


		Help help = method.getAnnotation(Help.class);
		if(help == null) {
			return loadUsage(subCommand, alreadyLoaded, null);
		}

		if (usage == null) {
			MethodVerifier.verifyHelpMethod(dispatcher, aClass, method);
			alreadyLoaded.addHelpCommand(dispatcher, (source, cmdHelp, page) -> {
				try {
					if (method.getParameters().length == 3) {
						method.invoke(instance, source, cmdHelp, page);
					} else {
						method.invoke(instance, source, cmdHelp);
					}
				} catch (IllegalAccessException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			});
			return null;
		} else {
			return loadUsage(null, alreadyLoaded, help);
		}

	}

	private CommandUsage<C> loadUsage(SubCommand subCommand,
	                                  Command<C> alreadyLoaded, Help help) {
		CommandUsage.Builder<C> builder = CommandUsage.builder();

		//setting permission
		Permission permission = method.getAnnotation(Permission.class);
		if (permission != null) {
			builder.permission(permission.value());
		}

		//loading cooldown
		Cooldown cooldown = method.getAnnotation(Cooldown.class);
		if (cooldown != null) {
			builder.cooldown(cooldown.value(), cooldown.unit());
		}

		//verifying parameters
		Parameter[] methodParams = method.getParameters();
		MethodVerifier.verifyMethod(dispatcher, aClass, method, false);

		//resolving parameters and execution
		int subtractionIndex = help != null ? 2 : 1;
		List<UsageParameter> usageParameters = new ArrayList<>(methodParams.length - subtractionIndex);
		var mainUsage = alreadyLoaded.getMainUsage();

		for (Parameter parameter : method.getParameters()) {
			UsageParameter usageParameter = getParameter(parameter, help);
			if (usageParameter == null) {
				continue;
			}
			if (subCommand != null
					  && mainUsage
					  .hasParameter((param) -> param.equals(usageParameter))) {
				continue;
			}
			usageParameters.add(usageParameter);
		}

		builder.parameters(usageParameters);

		final List<UsageParameter> fullParameters = new ArrayList<>(usageParameters.size() + mainUsage.getParameters().size());
		fullParameters.addAll(mainUsage.getParameters());
		fullParameters.addAll(usageParameters);

		builder.execute(
				  new MethodCommandExecutor<>(
				  instance, dispatcher, alreadyLoaded,
				  method, fullParameters, help
				  )
		);

		return builder.build();
	}



	private UsageParameter getParameter(Parameter parameter, Help help) {
		if(help != null && parameter.getType() == CommandHelp.class)return null;
		if (dispatcher.canBeSender(parameter.getType())) {
			return null;
		}

		Named named = parameter.getAnnotation(Named.class);
		Flag flag = parameter.getAnnotation(Flag.class);

		String name;

		Default defaultAnnotation = parameter.getAnnotation(Default.class);
		boolean hasDefault = defaultAnnotation != null;
		boolean optional = parameter.getAnnotation(Optional.class) != null;
		String defaultValue = hasDefault ? defaultAnnotation.value() : null;

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

		if(flag != null) {
			usageParameter = AnnotatedParameter.flag(name, parameter);
		}else {
			usageParameter = AnnotatedParameter.input(name, parameter.getType(), optional, greedy, defaultValue, parameter);
		}

		return usageParameter;
	}

}
