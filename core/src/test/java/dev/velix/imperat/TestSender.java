package dev.velix.imperat;

import dev.velix.imperat.caption.Caption;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;

public record TestSender(Object origin) implements Source {


    public void sendMsg(String msg) {
        System.out.println(msg);
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public void reply(String message) {

    }

    @Override
    public <S extends Source> void reply(Caption<S> caption, Context<S> context) {

    }

    @Override
    public <S extends Source> void reply(String prefix, Caption<S> caption, Context<S> context) {

    }

    @Override
    public boolean isConsole() {
        return false;
    }
}
