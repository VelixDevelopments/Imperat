package dev.velix.imperat.util.jeflect.parsers;

import dev.velix.imperat.util.jeflect.ByteClass;
import org.objectweb.asm.ClassReader;

/**
 * {@link ClassFileParser} implementation uses ASM.
 */
public final class AsmClassFileParser implements ClassFileParser {
	private static final int OPTIONS = ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES | ClassReader.SKIP_CODE;

	@Override
	public ByteClass parse(byte[] classFileBuffer) {
		var reader = new ClassReader(classFileBuffer);
		var parser = new ClassParser();
		reader.accept(parser, OPTIONS);
		return parser.getAsmClass();
	}
}
