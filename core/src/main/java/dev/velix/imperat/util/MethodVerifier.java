package dev.velix.imperat.util;

import dev.velix.imperat.CommandDispatcher;
import dev.velix.imperat.help.CommandHelp;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@ApiStatus.Internal
public final class MethodVerifier {

	public static <C> void verifyMethod(CommandDispatcher<C> dispatcher,
	                                    Class<?> clazz, Method method, boolean verifyDefault) {
		if(method.getReturnType() != void.class) {
			throw new IllegalStateException("In class '" + clazz.getName()  + "', this method '" + method.getName() + "' is not a void method");
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

	public static <C> void verifyHelpMethod(CommandDispatcher<C> dispatcher,
	                                    Class<?> clazz,Method method) {

		verifyMethod(dispatcher, clazz, method, false);
		var params = method.getParameters();
		if(params.length < 2 || params.length > 3 || params[1].getType() != CommandHelp.class) {
			throw new IllegalStateException("In class '" + clazz.getName() + "', Found help method '" + method.getName() + "' without parameter type of " + CommandHelp.class.getName());
		}
	}
}
