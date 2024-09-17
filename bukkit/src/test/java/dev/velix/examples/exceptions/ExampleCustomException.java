package dev.velix.examples.exceptions;

import dev.velix.Imperat;
import dev.velix.context.Context;
import dev.velix.context.Source;
import dev.velix.exception.SelfHandledException;

public final class ExampleCustomException extends SelfHandledException {
    
    private final String message;
    
    public ExampleCustomException(String message) {
        this.message = message;
    }
    
    @Override
    public <S extends Source> void handle(Imperat<S> imperat, Context<S> context) {
        var source = context.getSource();
        source.reply("<red>" + message);
    }
    
}
