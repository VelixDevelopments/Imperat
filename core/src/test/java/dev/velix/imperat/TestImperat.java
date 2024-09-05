package dev.velix.imperat;

import dev.velix.imperat.command.BaseImperat;
import dev.velix.imperat.resolvers.PermissionResolver;

import java.io.PrintStream;

public final class TestImperat extends BaseImperat<TestSender> {

    TestImperat() {
        super((source, permission) -> false);
    }

    @Override
    public String commandPrefix() {
        return "";
    }

    /**
     * @return {@link PermissionResolver} for the dispatcher
     */
    @Override
    public PermissionResolver<TestSender> getPermissionResolver() {
        return null;
    }

    /**
     * Wraps the sender into a built-in command-sender type
     *
     * @param sender the sender's actual value
     * @return the wrapped command-sender type
     */
    @Override
    public TestSender wrapSender(Object sender) {
        return new TestSender((PrintStream) sender);
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

}
