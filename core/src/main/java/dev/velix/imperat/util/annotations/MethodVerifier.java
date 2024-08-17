package dev.velix.imperat.util.annotations;

import dev.velix.imperat.CommandDispatcher;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.help.CommandHelp;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

@ApiStatus.Internal
public final class MethodVerifier {
	
	private MethodVerifier() {
	
	}
	
	public static boolean isMethodAcceptable(Method method) {
		if (method.getDeclaredAnnotations().length == 0
						|| method.getParameterCount() == 0) return false;
		return Modifier.isPublic(method.getModifiers());
	}
	
	public static <C> void verifyMethod(CommandDispatcher<C> dispatcher,
	                                    Class<?> clazz, Method method, boolean verifyDefault) {
		if (method.getReturnType() != void.class && Modifier.isPublic(method.getModifiers())) {
			throw methodError(clazz, method, "a return type (should be void)");
		}
		Parameter[] methodParams = method.getParameters();
		if (methodParams.length == 0) {
			throw methodError(clazz, method, "no parameters");
		}
		
		if (!dispatcher.canBeSender(methodParams[0].getType())) {
			throw methodError(clazz, method, "unsuitable command-sender type '" + methodParams[0].getType().getName());
		}
		
		if (verifyDefault && methodParams.length > 1) {
			throw methodError(clazz, method, "been treated as default-usage while having more than 1 input-parameter !");
		}
		
	}
	
	public static <C> void verifyHelpMethod(
					CommandDispatcher<C> dispatcher,
					CommandUsage<C> mainUsage,
					Class<?> clazz, Method method
	) {
		
		verifyMethod(dispatcher, clazz, method, false);
		
		Parameter commandHelpParam = null;
		int paramsCount = 0;
		for (Parameter parameter : method.getParameters()) {
			if (dispatcher.canBeSender(parameter.getType()) ||
							mainUsage.hasParamType(parameter.getType())) {
				
				if (CommandHelp.class.isAssignableFrom(parameter.getType()))
					commandHelpParam = parameter;
				
				continue;
			}
			paramsCount++;
		}
		if (paramsCount > 1) {
			throw helpMethodError(clazz, method, "more than one parameter types");
		}
		
		if (commandHelpParam == null) {
			throw helpMethodError(clazz, method, "no CommandHelp parameter");
		}
		
	}
	
	private static IllegalStateException helpMethodError(Class<?> clazz, Method method, String msg) {
		return methodError(clazz, method,
						msg + " while being treated as Help-Method");
	}
	
	private static IllegalStateException methodError(Class<?> clazz, Method method, String msg) {
		return new IllegalStateException(
						String.format("Method '%s' In class '%s' has "+ msg,
										method.getName(), clazz.getName()
						)
		);
	}
	
}
