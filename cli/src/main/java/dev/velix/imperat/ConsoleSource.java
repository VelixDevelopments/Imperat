package dev.velix.imperat;

import dev.velix.imperat.context.Source;

import java.io.PrintStream;

public final class ConsoleSource implements Source {
    
    private final PrintStream outputStream;
    
    public ConsoleSource(PrintStream outputStream) {
        this.outputStream = outputStream;
    }
    
    @Override
    public String name() {
        return "CONSOLE";
    }
    
    @Override
    public Object origin() {
        return outputStream;
    }
    
    @Override
    public void reply(String message) {
        outputStream.println(message);
    }
    
    @Override
    public void warn(String message) {
        outputStream.println("[WARN] " + message);
    }
    
    @Override
    public void error(String message) {
        outputStream.println("[ERROR] " + message);
    }
    
    @Override
    public boolean isConsole() {
        return true;
    }
}
