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
	
	public static boolean isMethodAcceptable(Method method) {
		if (method.getDeclaredAnnotations().length == 0
						|| method.getParameterCount() == 0) return false;
		return Modifier.isPublic(method.getModifiers());
	}
	
	public static <C> void verifyMethod(CommandDispatcher<C> dispatcher,
	                                    Class<?> clazz, Method method, boolean verifyDefault) {
		if (method.getReturnType() != void.class && Modifier.isPublic(method.getModifiers())) {
			throw new IllegalStateException("In class '" + clazz.getName() + "', this public method '" + method.getName() + "' is not a void method");
		}
		Parameter[] methodParams = method.getParameters();
		if (methodParams.length == 0) {
			throw new IllegalStateException("In class '" + clazz.getName() + "', this method '" + method.getName() + "' has no parameters !");
		}
		
		if (!dispatcher.canBeSender(methodParams[0].getType())) {
			throw new IllegalStateException("In class '" + clazz.getName() + "', unsuitable command-sender type '" + methodParams[0].getType().getName() + "' in method '" + method.getName() + "' , make sure your command sender is the first parameter");
		}
		
		
		if (verifyDefault && methodParams.length > 1) {
			throw new UnsupportedOperationException("In class '" + clazz.getName() + "', default-usage method '" + method.getName() + "' has more than 1 parameter !");
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
			throw new IllegalStateException("In class '" + clazz.getName() + "', Found help method '" + method.getName() + "' with more than one parameter types !");
		}
		
		if (commandHelpParam == null) {
			throw new IllegalStateException("In class '" + clazz.getName() + "', Found help method '" + method.getName() + "' without parameter type of " + CommandHelp.class.getName());
		}
	}
	
}
