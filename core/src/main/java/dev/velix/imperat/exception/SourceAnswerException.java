package dev.velix.imperat.exception;

import lombok.Getter;

@Getter
public class SourceAnswerException extends ImperatException {

    private final String message;
    private final AnswerType type;

    public SourceAnswerException(
            final String msg,
            final Object... args
    ) {
        this.type = AnswerType.ERROR;
        this.message = String.format(msg, args);
    }

    public SourceAnswerException(
            final AnswerType type,
            final String msg,
            final Object... args
    ) {
        this.type = type;
        this.message = String.format(msg, args);
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    public enum AnswerType {
        REPLY,
        WARN,
        ERROR
    }

}
