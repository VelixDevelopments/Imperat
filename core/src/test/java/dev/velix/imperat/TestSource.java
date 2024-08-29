package dev.velix.imperat;

import net.kyori.adventure.text.Component;

public class TestSource implements Source<TestSender> {

    private final TestSender sender;

    TestSource(TestSender sender) {
        this.sender = sender;
    }

    /**
     * @return name of command source
     */
    @Override
    public String getName() {
        return "";
    }

    /**
     * @return The original command sender type instance
     */
    @Override
    public TestSender getOrigin() {
        return sender;
    }

    /**
     * Replies to the command sender with a string message
     * this message is auto translated into a minimessage
     *
     * @param message the message
     */
    @Override
    public void reply(String message) {
        sender.sendMsg(message);
    }

    /**
     * Replies to the command sender with a chat component
     *
     * @param component the chat component
     */
    @Override
    public void reply(Component component) {

    }

    /**
     * @return Whether the command source is from the console
     */
    @Override
    public boolean isConsole() {
        return false;
    }


    @Override
    public <T> T as(Class<T> clazz) {
        return null;
    }
}