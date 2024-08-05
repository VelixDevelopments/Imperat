package dev.velix.imperat.util.jeflect.parsers;

import org.objectweb.asm.*;

final class ClassParser extends ClassVisitor {
    private AsmClass asmClass;

    ClassParser() {
        super(Opcodes.ASM8);
    }

    AsmClass getAsmClass() {
        return asmClass;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        asmClass = new AsmClass(superName, interfaces, name.replace('/', '.'), access);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return new AnnotationParser(descriptor, asmClass.annotations::add);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        var field = new AsmField(asmClass, descriptor, value, name, access);
        asmClass.fields.add(field);
        return new FieldParser(field);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        var method = new AsmMethod(asmClass, descriptor, exceptions, name, access);
        asmClass.methods.add(method);
        var type = Type.getType(descriptor);
        for (var parameter : type.getArgumentTypes()) {
            method.parameters.add(new AsmParameter(parameter.getDescriptor(), 0));
        }
        return new MethodParser(method);
    }
}
