package dev.velix.imperat.util.jeflect.parsers;

import dev.velix.imperat.util.jeflect.ByteAnnotation;
import dev.velix.imperat.util.jeflect.ByteClass;
import dev.velix.imperat.util.jeflect.ByteMethod;
import dev.velix.imperat.util.jeflect.ByteParameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class AsmMethod extends ByteMethod {
    final List<AsmParameter> parameters;
    final List<ByteAnnotation> annotations;

    AsmMethod(ByteClass parent, String descriptor, String[] exceptions, String name, int modifiers) {
        super(parent, descriptor, exceptions, name, modifiers);
        this.parameters = new ArrayList<>();
        this.annotations = new ArrayList<>();
    }

    @Override
    public List<ByteAnnotation> getAnnotations() {
        return Collections.unmodifiableList(annotations);
    }

    @Override
    public List<ByteParameter> getParameters() {
        return Collections.unmodifiableList(parameters);
    }
}
