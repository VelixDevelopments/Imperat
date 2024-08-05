package dev.velix.imperat.util.jeflect.parsers;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

final class FieldParser extends FieldVisitor {
    private final AsmField field;

    FieldParser(AsmField field) {
        super(Opcodes.ASM8);
        this.field = field;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return new AnnotationParser(descriptor, field.annotations::add);
    }
}
