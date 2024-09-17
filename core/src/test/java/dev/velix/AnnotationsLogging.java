package dev.velix;


import dev.velix.annotations.base.AnnotationReader;
import dev.velix.annotations.base.AnnotationRegistry;
import dev.velix.annotations.base.element.CommandClassVisitor;
import dev.velix.commands.annotations.TestCommand;
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
