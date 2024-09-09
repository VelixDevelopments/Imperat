package dev.velix.imperat;

import dev.velix.imperat.command.BaseImperat;
import dev.velix.imperat.command.Command;

import java.io.PrintStream;

public final class TestImperat extends BaseImperat<TestSource> {

    TestImperat() {
        super((source, permission) -> true);
    }

    @Override
    public String commandPrefix() {
        return "/";
    }

    /**
     * Wraps the sender into a built-in command-sender type
     *
     * @param sender the sender's actual value
     * @return the wrapped command-sender type
     */
    @Override
    public TestSource wrapSender(Object sender) {
        return new TestSource((PrintStream) sender);
    }

    /**
     * @return the platform of the module
     */
    @Override
    public Object getPlatform() {
        return null;
    }


    /**
     *
     */
    @Override
    public void shutdownPlatform() {

    }
    
    @Override
    public void registerCommand(Command<TestSource> command) {
        super.registerCommand(command);
        command.visualize();
    }
}
