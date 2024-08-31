package dev.velix.imperat.examples.exceptions;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.exceptions.CommandException;

public final class ExampleCustomException extends CommandException {


    public ExampleCustomException(String msg) {
        super(msg);
    }

    @Override
    public <C> void handle(Imperat<C> imperat, Context<C> context) {
        var source = context.getSource();
        source.reply("<red>" + msg);
    }

}
