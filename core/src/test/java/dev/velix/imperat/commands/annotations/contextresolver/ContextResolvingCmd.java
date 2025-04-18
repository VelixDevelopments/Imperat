package dev.velix.imperat.commands.annotations.contextresolver;


import dev.velix.imperat.TestSource;
import dev.velix.imperat.annotations.Command;
import dev.velix.imperat.annotations.ContextResolved;
import dev.velix.imperat.annotations.SubCommand;
import dev.velix.imperat.annotations.Usage;
import dev.velix.imperat.commands.annotations.examples.Group;
import org.junit.jupiter.api.Assertions;

@Command("ctx")
public final class ContextResolvingCmd {

    @Usage
    public void def(TestSource source, @ContextResolved PlayerData data) {
        Assertions.assertEquals(source.name(), data.name());
    }

    @SubCommand("sub")
    public void defSub(TestSource source, @ContextResolved Group group) {
        //throws an error
        System.out.println("DEFAULT SUBCMD EXECUTION, CONTEXT RESOLVED GROUP=" + group.name());
    }

}
