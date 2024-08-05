package dev.velix.imperat.annotations.loaders;

import dev.velix.imperat.CommandDispatcher;
import dev.velix.imperat.annotations.AnnotationLoader;
import dev.velix.imperat.annotations.types.Permission;
import dev.velix.imperat.annotations.types.methods.Cooldown;
import dev.velix.imperat.annotations.types.methods.DefaultUsage;
import dev.velix.imperat.annotations.types.methods.SubCommand;
import dev.velix.imperat.annotations.types.methods.Usage;
import dev.velix.imperat.annotations.types.parameters.*;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.UsageParameter;
import dev.velix.imperat.util.MethodVerifier;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public final class CommandUsageLoader<C> implements AnnotationLoader<C, CommandUsage<C>> {

	private final CommandDispatcher<C> dispatcher;
	private final Class<?> aClass;
	private final Object instance;
	private final Method method;

	public CommandUsageLoader(CommandDispatcher<C> dispatcher,
	                          Object instance,
	                          Class<?> clazz, Method method) {
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

		if (usage == null && subCommand == null) {
			if (method.getAnnotation(DefaultUsage.class) != null) {
				MethodVerifier.verifyMethod(dispatcher, aClass, method, true);
				alreadyLoaded.setDefaultUsageExecution((source, context) -> {
					try {
						method.invoke(instance, source);
					} catch (IllegalAccessException | InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				});
				return null;
			}
			//System.out.println("NO USAGE ANNOTATION !");
			throw new IllegalStateException("Couldn't find @Usage or @SubCommand in method '" + method.getName() + "' in class '" + aClass.getName() + "'");
		} else if (usage != null && subCommand != null) {
			throw new IllegalStateException("Found annotation duplicates of similar context '@Usage' and '@SubCommand' in the same method !");
		}

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
		Object[] paramsInstances = new Object[methodParams.length];

		List<UsageParameter> usageParameters = new ArrayList<>(methodParams.length - 1);
		var mainUsage = alreadyLoaded.getMainUsage();

		for (Parameter parameter : method.getParameters()) {
			UsageParameter usageParameter = getParameter(parameter);
			if (usageParameter == null) {
				continue;
			}
			if (subCommand != null
					  && mainUsage
					  .hasParameter((param) -> param.equals(usageParameter))) {
				continue;
			}
			System.out.println("Passed " + usageParameter.getName());
			usageParameters.add(usageParameter);
		}

		builder.parameters(usageParameters);
		final List<UsageParameter> fullParameters = new ArrayList<>(usageParameters.size() + mainUsage.getParameters().size());
		fullParameters.addAll(mainUsage.getParameters());
		fullParameters.addAll(usageParameters);


		builder.execute(((commandSource, context) -> {
			paramsInstances[0] = commandSource;
			for (int i = 1, p = 0; i < paramsInstances.length; i++, p++) {
				UsageParameter parameter = getUsageParam(fullParameters, p);
				if (parameter == null) continue;
				paramsInstances[i] = parameter.isFlag() ? context.getFlag(parameter.getName()) : context.getArgument(parameter.getName());
			}

			try {
				method.invoke(instance, paramsInstances);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}));

		return builder.build();
	}

	private @Nullable UsageParameter getUsageParam(List<UsageParameter> params, int index) {
		try {
			return params.get(index);
		} catch (Exception ex) {
			return null;
		}
	}

	private UsageParameter getParameter(Parameter parameter) {
		if (dispatcher.canBeSender(parameter.getType())) {
			return null;
		}

		Arg arg = parameter.getAnnotation(Arg.class);
		Flag flag = parameter.getAnnotation(Flag.class);

		String name;


		Default defaultAnnotation = parameter.getAnnotation(Default.class);
		boolean hasDefault = defaultAnnotation != null;
		boolean optional = parameter.getAnnotation(Optional.class) != null;
		String defaultValue = hasDefault ? defaultAnnotation.value() : null;

		if (arg != null) {
			name = arg.value();
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
		if (greedy) {
			usageParameter = UsageParameter.greedy(name, optional, defaultValue);
		} else if (optional) {
			usageParameter = UsageParameter.optional(name, parameter.getType(), defaultValue);
		} else if (flag != null) {
			usageParameter = UsageParameter.flag(flag.value());
		} else {
			usageParameter = UsageParameter.required(name, parameter.getType());
		}

		return usageParameter;
	}

}
