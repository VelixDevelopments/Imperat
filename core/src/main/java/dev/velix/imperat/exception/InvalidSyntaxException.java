package dev.velix.imperat.exception;

import dev.velix.imperat.command.tree.CommandDispatch;
import dev.velix.imperat.context.Source;

public final class InvalidSyntaxException extends ImperatException {

    private final CommandDispatch<?> result;
    
    public <S extends Source> InvalidSyntaxException(CommandDispatch<S> result) {
        this.result = result;
    }
    
    public CommandDispatch<?> getExecutionResult() {
        return result;
    }
}
