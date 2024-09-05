package dev.velix.imperat.examples.exceptions;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.exceptions.CommandException;

public final class ExampleCustomException extends CommandException {


    public ExampleCustomException(String msg) {
        super(msg);
    }

    @Override
    public <S extends Source> void handle(Imperat<S> imperat, Context<S> context) {
        var source = context.getSource();
        source.reply("<red>" + msg);
    }

}
