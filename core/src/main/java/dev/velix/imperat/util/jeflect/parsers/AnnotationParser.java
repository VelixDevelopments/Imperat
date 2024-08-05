package dev.velix.imperat.util.jeflect.parsers;

import dev.velix.imperat.util.jeflect.ByteAnnotation;
import dev.velix.imperat.util.jeflect.EnumEntry;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

final class AnnotationParser extends AnnotationVisitor {
    private final String className;
    private final Consumer<ByteAnnotation> finalizer;
    private final Map<String, Object> fields;

    AnnotationParser(String descriptor, Consumer<ByteAnnotation> finalizer) {
        super(Opcodes.ASM8);
        this.className = Type.getType(descriptor).getClassName();
        this.finalizer = finalizer;
        this.fields = new HashMap<>();
    }

    @Override
    public void visit(String name, Object value) {
        fields.put(name, value);
    }

    @Override
    public void visitEnum(String name, String descriptor, String value) {
        var type = Type.getType(descriptor);
        fields.put(name, new EnumEntry(type.getClassName(), value));
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        return new ArrayParser(name);
    }

    @Override
    public void visitEnd() {
        finalizer.accept(new ByteAnnotation(className, fields));
    }

    private class ArrayParser extends AnnotationVisitor {
        private final String name;

        private ArrayParser(String name) {
            super(Opcodes.ASM8);
            this.name = name;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void visit(String name, Object value) {
            var values = (List<Object>) fields.computeIfAbsent(this.name, v -> new LinkedList<>());
            values.add(value);
        }

        @Override
        public void visitEnum(String name, String descriptor, String value) {
            var type = Type.getType(descriptor);
            this.visit(name, new EnumEntry(type.getClassName(), value));
        }

        @Override
        @SuppressWarnings("unchecked")
        public void visitEnd() {
            var values = (List<Object>) fields.get(name);
            fields.put(name, values.toArray());
        }
    }
}
