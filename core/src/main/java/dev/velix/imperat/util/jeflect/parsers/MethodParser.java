package dev.velix.imperat.util.jeflect.parsers;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

final class MethodParser extends MethodVisitor {
    private final AsmMethod method;

    MethodParser(AsmMethod method) {
        super(Opcodes.ASM8);
        this.method = method;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return new AnnotationParser(descriptor, method.annotations::add);
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        var asmParameter = method.parameters.get(parameter);
        return new AnnotationParser(descriptor, asmParameter.annotations::add);
    }
}
