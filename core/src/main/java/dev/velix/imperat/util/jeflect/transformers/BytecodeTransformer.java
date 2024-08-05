package dev.velix.imperat.util.jeflect.transformers;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.security.ProtectionDomain;
import java.util.Objects;
import java.util.function.Function;


public final class BytecodeTransformer extends CheckedTransformer {
	private static final int DEFAULT_READER_OPTIONS = ClassReader.SKIP_FRAMES;
	private static final int DEFAULT_WRITER_OPTIONS = ClassWriter.COMPUTE_FRAMES;
	private final int readerOptions;
	private final int writerOptions;
	private final VisitorProvider provider;

	public BytecodeTransformer(int readerOptions, int writerOptions, VisitorProvider provider) {
		this.readerOptions = readerOptions;
		this.writerOptions = writerOptions;
		this.provider = Objects.requireNonNull(provider);
	}

	public BytecodeTransformer(int readerOptions,
	                           int writerOptions,
	                           String className,
	                           Function<ClassVisitor, ClassVisitor> provider) {
		this(readerOptions, writerOptions, name -> {
			if (className.equals(name)) {
				return provider;
			}
			return null;
		});
	}

	public BytecodeTransformer(String className, Function<ClassVisitor, ClassVisitor> provider) {
		this(DEFAULT_READER_OPTIONS, DEFAULT_WRITER_OPTIONS, className, provider);
	}

	@Override
	protected byte[] checkedTransform(ClassLoader loader,
	                                  String className,
	                                  Class<?> classBeingRedefined,
	                                  ProtectionDomain protectionDomain,
	                                  byte[] classfileBuffer) {
		var constructor = provider.get(className);
		if (constructor == null) {
			return classfileBuffer;
		}
		var reader = new ClassReader(classfileBuffer);
		var writer = new ClassWriter(reader, writerOptions);
		var visitor = constructor.apply(writer);
		reader.accept(visitor, readerOptions);
		return writer.toByteArray();
	}
}
