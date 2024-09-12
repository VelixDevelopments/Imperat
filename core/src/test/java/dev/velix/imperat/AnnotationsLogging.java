package dev.velix.imperat;


import dev.velix.imperat.annotations.AnnotationReader;
import dev.velix.imperat.annotations.AnnotationRegistry;
import dev.velix.imperat.annotations.element.CommandClassVisitor;
import dev.velix.imperat.commands.annotations.TestCommand;
import org.junit.jupiter.api.Test;


public final class AnnotationsLogging {
    
    private final Imperat<TestSource> imperat = new TestImperat();
    private final AnnotationRegistry registry = new AnnotationRegistry();
    private final CommandClassVisitor<TestSource> visitor = CommandClassVisitor.newSimpleVisitor(imperat);
    
    @Test
    public void debugVisitor() {
        TestCommand cmd = new TestCommand();
        AnnotationReader<TestSource> reader = AnnotationReader.read(imperat, registry, cmd);
        reader.accept(imperat, visitor);
    }
    
}
