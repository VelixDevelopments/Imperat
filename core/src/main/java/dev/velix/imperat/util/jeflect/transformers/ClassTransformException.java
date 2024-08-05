package dev.velix.imperat.util.jeflect.transformers;

import java.util.List;

public class ClassTransformException extends IllegalStateException {
    private final List<Throwable> problems;

    public ClassTransformException(List<Throwable> problems) {
        super("Cannot finalize class transforming due to unexpected exceptions");
        this.problems = problems;
    }

    public List<Throwable> getProblems() {
        return problems;
    }
}
