package dev.velix.imperat.util.jeflect.fields;


import com.github.romanqed.jfunc.Exceptions;
import dev.velix.imperat.util.jeflect.AsmUtil;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

final class FieldUtil {
    private static final Class<FieldAccessor> ACCESSOR = FieldAccessor.class;
    private static final Method GET = Exceptions.suppress(() -> ACCESSOR.getDeclaredMethod("get", Object.class));
    private static final Method STATIC_GET = Exceptions.suppress(() -> ACCESSOR.getDeclaredMethod("get"));
    private static final Method SET = Exceptions.suppress(
            () -> ACCESSOR.getDeclaredMethod("set", Object.class, Object.class)
    );
    private static final Method STATIC_SET = Exceptions.suppress(
            () -> ACCESSOR.getDeclaredMethod("set", Object.class)
    );

    private FieldUtil() {
    }

    static void createGet(MethodVisitor visitor, Type owner, String name, Type type, boolean isStatic) {
        // Open method
        visitor.visitCode();
        if (!isStatic) {
            // Load object
            visitor.visitVarInsn(Opcodes.ALOAD, 1);
            // Cast to field owner
            visitor.visitTypeInsn(Opcodes.CHECKCAST, owner.getInternalName());
        }
        // Load field value
        visitor.visitFieldInsn(isStatic ? Opcodes.GETSTATIC : Opcodes.GETFIELD,
                owner.getInternalName(),
                name,
                type.getDescriptor());
        // Pack primitive
        AsmUtil.packPrimitive(visitor, type);
        // Return value
        visitor.visitInsn(Opcodes.ARETURN);
        // Close method
        visitor.visitMaxs(0, 0);
        visitor.visitEnd();
    }

    static void createSet(MethodVisitor visitor, Type owner, String name, Type type, boolean isStatic) {
        // Open method
        visitor.visitCode();
        var index = 1;
        if (!isStatic) {
            // Load object
            visitor.visitVarInsn(Opcodes.ALOAD, index++);
            // Cast to field owner
            visitor.visitTypeInsn(Opcodes.CHECKCAST, owner.getInternalName());
        }
        // Load field value from parameter
        visitor.visitVarInsn(Opcodes.ALOAD, index);
        // Cast value
        AsmUtil.castReference(visitor, type);
        // Set value to field
        visitor.visitFieldInsn(isStatic ? Opcodes.PUTSTATIC : Opcodes.PUTFIELD,
                owner.getInternalName(),
                name,
                type.getDescriptor());
        // Return
        visitor.visitInsn(Opcodes.RETURN);
        // Close method
        visitor.visitMaxs(0, 0);
        visitor.visitEnd();
    }

    static byte[] createAccessor(String name, Field field) {
        // Create accessor class
        var writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        writer.visit(Opcodes.V11,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                name,
                null,
                Type.getInternalName(Object.class),
                new String[]{Type.getInternalName(ACCESSOR)});
        // Create empty constructor
        AsmUtil.createEmptyConstructor(writer);
        var modifiers = field.getModifiers();
        var isStatic = Modifier.isStatic(modifiers);
        // Prepare data
        var owner = Type.getType(field.getDeclaringClass());
        var type = Type.getType(field.getType());
        // Implement get method
        var getMethod = isStatic ? STATIC_GET : GET;
        var get = writer.visitMethod(Opcodes.ACC_PUBLIC,
                getMethod.getName(),
                Type.getMethodDescriptor(getMethod),
                null,
                null);
        createGet(get, owner, field.getName(), type, isStatic);
        // Check if field is final
        if (Modifier.isFinal(modifiers)) {
            // Close writer
            writer.visitEnd();
            return writer.toByteArray();
        }
        // Implement set method
        var setMethod = isStatic ? STATIC_SET : SET;
        var set = writer.visitMethod(Opcodes.ACC_PUBLIC,
                setMethod.getName(),
                Type.getMethodDescriptor(setMethod),
                null,
                null);
        createSet(set, owner, field.getName(), type, isStatic);
        // Close writer
        writer.visitEnd();
        return writer.toByteArray();
    }
}
