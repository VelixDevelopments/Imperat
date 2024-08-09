package dev.velix.imperat.annotations;

import dev.velix.imperat.CommandDispatcher;
import dev.velix.imperat.CommandSource;
import dev.velix.imperat.annotations.types.methods.Help;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandExecution;
import dev.velix.imperat.command.parameters.UsageParameter;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.help.CommandHelp;
import dev.velix.imperat.util.reflection.DefaultMethodCallerFactory;
import dev.velix.imperat.util.reflection.MethodCaller;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

public final class MethodCommandExecutor<C> implements CommandExecution<C> {

	private final CommandDispatcher<C> dispatcher;
	private final Command<C> command;
	private final Method method;
	private final MethodCaller.BoundMethodCaller boundMethodCaller;
	private final List<UsageParameter> fullParameters;
	private final Help helpAnnotation;

	public MethodCommandExecutor(Object proxy,
	                             CommandDispatcher<C> dispatcher,
	                             @Nullable Command<C> command,
	                             Method method,
	                             List<UsageParameter> fullParameters,
	                             Help help) {
		this.dispatcher = dispatcher;
		this.command = command;
		this.method = method;
		try {
			boundMethodCaller = DefaultMethodCallerFactory.INSTANCE.createFor(method).bindTo(proxy);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		this.fullParameters = fullParameters;
		this.helpAnnotation = help;
	}

	@SuppressWarnings("unchecked")
	private Object[] loadParameterInstances(CommandSource<C> source, ExecutionContext context, Method method) {
		Parameter[] parameters = method.getParameters();
		Object[] paramsInstances = new Object[parameters.length];

		paramsInstances[0] = source;

		for (int i = 1, p = 0; i < paramsInstances.length; i++, p++) {
			Parameter actualParameter = parameters[i];

			var factory = dispatcher.getContextResolverFactory();
			var contextResolver = factory.create(actualParameter);

			if(contextResolver != null) {
				paramsInstances[i] = contextResolver.resolve((Context<C>) context, actualParameter);
				continue;
			}
			contextResolver = dispatcher.getContextResolver(actualParameter.getType());
			if(contextResolver != null){
				paramsInstances[i] = contextResolver.resolve((Context<C>) context, actualParameter);
			}


			if(helpAnnotation != null && actualParameter.getType() == CommandHelp.class) {
				paramsInstances[i] = new CommandHelp<>(dispatcher, command, (Context<C>) context, null);
				p--;
				continue;
			}

			UsageParameter parameter = getUsageParam(fullParameters, p);
			if(parameter == null) continue;
			paramsInstances[i] = parameter.isFlag() ?
					  context.getFlag(parameter.getName())
					  : context.getArgument(parameter.getName());
		}

		return paramsInstances;
	}

	/**
	 * Executes the command's actions
	 *
	 * @param commandSource the source/sender of this command
	 * @param context       the context of the command
	 */
	@Override
	public void execute(CommandSource<C> commandSource,
	                    ExecutionContext context) {
		boundMethodCaller.call(loadParameterInstances(commandSource, context, method));
	}

	private @Nullable UsageParameter getUsageParam(List<UsageParameter> params, int index) {
		try {
			return params.get(index);
		} catch (Exception ex) {
			return null;
		}
	}

}
