package dev.velix.imperat.util.jeflect.parsers;


import dev.velix.imperat.util.jeflect.ByteAnnotation;
import dev.velix.imperat.util.jeflect.ByteClass;
import dev.velix.imperat.util.jeflect.ByteField;
import dev.velix.imperat.util.jeflect.ByteMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class AsmClass extends ByteClass {
    final List<ByteAnnotation> annotations;
    final List<ByteField> fields;
    final List<ByteMethod> methods;

    AsmClass(String superType, String[] interfaces, String name, int modifiers) {
        super(superType, interfaces, name, modifiers);
        this.annotations = new ArrayList<>();
        this.fields = new ArrayList<>();
        this.methods = new ArrayList<>();
    }

    public List<ByteAnnotation> getAnnotations() {
        return Collections.unmodifiableList(annotations);
    }

    @Override
    public List<ByteField> getFields() {
        return Collections.unmodifiableList(fields);
    }


    @Override
    public List<ByteMethod> getMethods() {
        return Collections.unmodifiableList(methods);
    }
}
