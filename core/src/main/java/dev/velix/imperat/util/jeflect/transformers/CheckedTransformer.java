package dev.velix.imperat.util.jeflect.transformers;


import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public abstract class CheckedTransformer implements ClassFileTransformer {
	private final Supplier<List<Throwable>> supplier;
	private List<Throwable> problems;

	protected CheckedTransformer(Supplier<List<Throwable>> supplier) {
		this.supplier = supplier;
		this.problems = Collections.synchronizedList(supplier.get());
	}

	protected CheckedTransformer() {
		this(LinkedList::new);
	}

	public synchronized void validate() {
		if (!problems.isEmpty()) {
			throw new ClassTransformException(problems);
		}
		this.problems = Collections.synchronizedList(supplier.get());
	}

	protected abstract byte[] checkedTransform(ClassLoader loader,
	                                           String className,
	                                           Class<?> classBeingRedefined,
	                                           ProtectionDomain protectionDomain,
	                                           byte[] classfileBuffer) throws Throwable;

	@Override
	public byte[] transform(ClassLoader loader,
	                        String className,
	                        Class<?> classBeingRedefined,
	                        ProtectionDomain protectionDomain,
	                        byte[] classfileBuffer) {
		try {
			return checkedTransform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
		} catch (Throwable e) {
			problems.add(e);
			return classfileBuffer;
		}
	}
}
