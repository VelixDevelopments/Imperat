package dev.velix.imperat.annotations.base;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodOrderHelper {
    public static List<Method> getMethodsInSourceOrder(Class<?> clazz) throws Exception {
        String className = clazz.getName().replace('.', '/') + ".class";
        try (InputStream stream = clazz.getClassLoader().getResourceAsStream(className)) {
            if (stream == null) {
                throw new IOException("Class resource not found: " + className);
            }

            // First pass: Get method order from ASM
            ClassReader reader = new ClassReader(stream);
            MethodOrderVisitor visitor = new MethodOrderVisitor();
            reader.accept(visitor, ClassReader.SKIP_DEBUG);

            // Create map of method signatures to reflection Method objects
            Map<String, Method> methodMap = new HashMap<>();
            for (Method method : clazz.getDeclaredMethods()) {
                String key = method.getName() + Type.getMethodDescriptor(method);
                methodMap.put(key, method);
            }

            // Build ordered list based on ASM visitation order
            List<Method> orderedMethods = new ArrayList<>();
            for (MethodSignature signature : visitor.getMethodSignatures()) {
                Method method = methodMap.get(signature.name + signature.descriptor);
                if (method != null && !method.isSynthetic()) {
                    orderedMethods.add(method);
                }
            }

            return orderedMethods;
        }
    }

    private static class MethodOrderVisitor extends ClassVisitor {
        private final List<MethodSignature> methodSignatures = new ArrayList<>();

        public MethodOrderVisitor() {
            super(Opcodes.ASM9);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            // Record method signature in visitation order
            methodSignatures.add(new MethodSignature(name, descriptor));
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }

        public List<MethodSignature> getMethodSignatures() {
            return methodSignatures;
        }
    }

    private static class MethodSignature {
        final String name;
        final String descriptor;

        MethodSignature(String name, String descriptor) {
            this.name = name;
            this.descriptor = descriptor;
        }
    }
}