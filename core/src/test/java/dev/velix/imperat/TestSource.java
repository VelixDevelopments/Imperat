package dev.velix.imperat;

import dev.velix.imperat.caption.Caption;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;

import java.io.PrintStream;

public record TestSource(PrintStream origin) implements Source {


    public void sendMsg(String msg) {
        origin.println(msg);
    }

    @Override
    public String name() {
        return "CONSOLE";
    }

    @Override
    public void reply(String message) {
        sendMsg(message);
    }

    @Override
    public void error(String message) {
        sendMsg(message);
    }

    @Override
    public <S extends Source> void reply(Caption<S> caption, Context<S> context) {

    }

    @Override
    public <S extends Source> void reply(String prefix, Caption<S> caption, Context<S> context) {

    }

    @Override
    public boolean isConsole() {
        return true;
    }
}
