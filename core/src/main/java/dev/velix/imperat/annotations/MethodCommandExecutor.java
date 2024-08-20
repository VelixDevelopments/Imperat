package dev.velix.imperat.annotations;

import dev.velix.imperat.CommandDebugger;
import dev.velix.imperat.CommandDispatcher;
import dev.velix.imperat.CommandSource;
import dev.velix.imperat.command.CommandExecution;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.internal.ResolvedFlag;
import dev.velix.imperat.help.CommandHelp;
import dev.velix.imperat.resolvers.OptionalValueSupplier;
import dev.velix.imperat.util.reflection.DefaultMethodCallerFactory;
import dev.velix.imperat.util.reflection.MethodCaller;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

@ApiStatus.Internal
public final class MethodCommandExecutor<C> implements CommandExecution<C> {
	
	private final Object proxy;
	private final CommandDispatcher<C> dispatcher;
	private final Method method;
	private final MethodCaller.BoundMethodCaller boundMethodCaller;
	private final List<? extends CommandParameter> fullParameters;
	//private final Help helpAnnotation;
	
	public MethodCommandExecutor(Object proxy,
	                             CommandDispatcher<C> dispatcher,
	                             Method method,
	                             List<? extends CommandParameter> fullParameters
					/*Help help*/) {
		this.proxy = proxy;
		this.dispatcher = dispatcher;
		this.method = method;
		try {
			boundMethodCaller = DefaultMethodCallerFactory.INSTANCE.createFor(method).bindTo(proxy);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		this.fullParameters = fullParameters;
		//this.helpAnnotation = help;
	}
	
	@SuppressWarnings("unchecked")
	public static <C> Object[] loadParameterInstances(CommandDispatcher<C> dispatcher,
																										 List<? extends CommandParameter> fullParameters,
	                                                   CommandSource<C> source,
	                                                   ExecutionContext context,
	                                                   Method method,
	                                                  @Nullable CommandHelp<C> commandHelp) {
		Parameter[] parameters = method.getParameters();
		Object[] paramsInstances = new Object[parameters.length];
		
		paramsInstances[0] = source;
		
		for (int i = 1, p = 0; i < parameters.length; i++, p++) {
			Parameter actualParameter = parameters[i];
			
			if(commandHelp != null && CommandHelp.class.isAssignableFrom(actualParameter.getType())) {
				paramsInstances[i] = commandHelp;
				p--;
				continue;
			}
			
			var factory = dispatcher.getContextResolverFactory();
			var contextResolver = factory.create(actualParameter);
			
			if (contextResolver != null) {
				paramsInstances[i] = contextResolver.resolve((Context<C>) context, actualParameter);
				continue;
			}
			
			contextResolver = dispatcher.getContextResolver(actualParameter.getType());
			if (contextResolver != null) {
				paramsInstances[i] = contextResolver.resolve((Context<C>) context, actualParameter);
				continue;
			}
			
			CommandParameter parameter = getUsageParam(fullParameters, p);
			
			if (parameter == null)
				continue;
			
			if(parameter.isFlag()) {
				ResolvedFlag value = context.getFlag(parameter.getName());
				paramsInstances[i] = value.value();
			}else {
				
				Object value = context.getArgument(parameter.getName());
				if(value == null && parameter.isOptional()) {
					OptionalValueSupplier<?> valueSupplier = parameter.getDefaultValueSupplier();
					if(valueSupplier != null) {
						value = valueSupplier.supply((Context<C>) context);
					}
				}
				paramsInstances[i] = value;
			}
			
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
		
		var instances = loadParameterInstances(dispatcher, fullParameters,
						commandSource, context, method, null);
		try {
			boundMethodCaller.call(instances);
		}catch (Exception ex) {
			CommandDebugger.error(proxy.getClass(), method.getName(), ex);
		}
		
	}
	
	
	private static @Nullable CommandParameter getUsageParam(List<? extends CommandParameter> params, int index) {
		if(index < 0 || index >= params.size()) return null;
		return params.get(index);
	}
	
}
