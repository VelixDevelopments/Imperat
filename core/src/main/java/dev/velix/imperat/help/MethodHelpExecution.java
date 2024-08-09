package dev.velix.imperat.help;

import dev.velix.imperat.CommandSource;
import dev.velix.imperat.util.reflection.DefaultMethodCallerFactory;
import dev.velix.imperat.util.reflection.MethodCaller;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public final class MethodHelpExecution<C> implements HelpExecution<C> {

	private final Method method;
	private final MethodCaller.BoundMethodCaller caller;

	public MethodHelpExecution(Object proxy, Method method) {
		this.method = method;
		try {
			this.caller = DefaultMethodCallerFactory.INSTANCE
					  .createFor(method).bindTo(proxy);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Displays a help menu showing all possible syntaxes
	 *
	 * @param source the source of this execution
	 * @param help   the help object
	 * @param page   the page of the help menu
	 */
	@Override
	public void help(CommandSource<C> source,
	                 CommandHelp<C> help,
	                 @Nullable Integer page) {

		int length = method.getParameterCount();
		if (length == 3) {
			assert page != null;
			caller.call(source, help, page);
		} else {
			caller.call(source, help);
		}

	}
}
