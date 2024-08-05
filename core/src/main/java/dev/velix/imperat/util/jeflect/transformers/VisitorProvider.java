package dev.velix.imperat.util.jeflect.transformers;

import org.objectweb.asm.ClassVisitor;

import java.util.function.Function;

@FunctionalInterface
public interface VisitorProvider {
	Function<ClassVisitor, ClassVisitor> get(String className);
}
