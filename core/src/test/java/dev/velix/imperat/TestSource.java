package dev.velix.imperat;

import dev.velix.imperat.context.Source;

import java.io.PrintStream;

public record TestSource(PrintStream origin) implements Source {
    
    public void sendMsg(String msg) {
        origin.println(msg);
    }
    
    @Override
    public String name() {
        return "mqzen";
    }
    
    @Override
    public void reply(String message) {
        sendMsg(message);
    }
    
    @Override
    public void warn(String message) {
        sendMsg(message);
    }
    
    @Override
    public void error(String message) {
        sendMsg(message);
    }
    
    @Override
    public boolean isConsole() {
        return true;
    }
    
}
