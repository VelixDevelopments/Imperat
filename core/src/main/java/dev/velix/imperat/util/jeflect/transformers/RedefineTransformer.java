package dev.velix.imperat.util.jeflect.transformers;


import dev.velix.imperat.util.jeflect.ByteClass;
import dev.velix.imperat.util.jeflect.parsers.AsmClassFileParser;
import dev.velix.imperat.util.jeflect.parsers.ClassFileParser;

import java.security.ProtectionDomain;

public abstract class RedefineTransformer extends CheckedTransformer {
	private static final String IMMUTABLE_PREFIX = "java";
	private final ClassFileParser parser;

	protected RedefineTransformer(ClassFileParser parser) {
		this.parser = parser;
	}

	protected RedefineTransformer() {
		this(new AsmClassFileParser());
	}

	protected abstract byte[] transform(ClassLoader loader,
	                                    ByteClass byteClass,
	                                    ProtectionDomain domain,
	                                    byte[] classFileBuffer) throws Throwable;

	@Override
	protected byte[] checkedTransform(ClassLoader loader,
	                                  String className,
	                                  Class<?> classBeingRedefined,
	                                  ProtectionDomain protectionDomain,
	                                  byte[] classfileBuffer) throws Throwable {
		if (classBeingRedefined != null) {
			throw new IllegalStateException("It is not possible to change the class because it is already loaded");
		}
		if (className != null && className.startsWith(IMMUTABLE_PREFIX)) {
			return classfileBuffer;
		}
		var byteClass = parser.parse(classfileBuffer);
		return transform(loader, byteClass, protectionDomain, classfileBuffer);
	}
}
