package dev.velix.imperat;

import java.util.logging.*;

public final class CommandDebugger {
	
	//private final static String LOGGER_NAME = "Imperat";
	private static Logger LOGGER;
	
	public static void setLogger(Logger LOGGER) {
		CommandDebugger.LOGGER = LOGGER;
	}
	
	private CommandDebugger() {
	}
	
	public static void debug(String msg, Object... args) {
		LOGGER.log(Level.INFO, String.format(msg, args));
	}
	
	public static void warning(String msg, Object... args){
		LOGGER.log(Level.WARNING, String.format(msg, args));
	}
	
	public static void error(Class<?> owningClass, String name, Throwable ex) {
	  LOGGER.log(Level.SEVERE, String.format("Error in class '%s', in method '%s'", owningClass.getName(), name), ex);
	}

}
