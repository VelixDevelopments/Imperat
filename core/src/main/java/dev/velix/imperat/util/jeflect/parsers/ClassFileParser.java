package dev.velix.imperat.util.jeflect.parsers;


import dev.velix.imperat.util.jeflect.ByteClass;

/**
 * A class representing a bytecode parser that will return the analysis results as a byte reflection instance.
 */
public interface ClassFileParser {

    /**
     * Parses the bytecode of the class.
     *
     * @param classFileBuffer buffer containing class bytecode
     * @return {@link ByteClass} instance containing parsed class data
     */
    ByteClass parse(byte[] classFileBuffer);
}
