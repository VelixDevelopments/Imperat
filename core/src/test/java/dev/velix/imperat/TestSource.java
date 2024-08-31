package dev.velix.imperat;

import dev.velix.imperat.caption.Caption;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;

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
     * Replies to the command sender with a caption message
     *
     * @param caption the {@link Caption} to send
     * @param context the {@link Context} to use
     */
    @Override
    public void reply(Caption<TestSender> caption, Context<TestSender> context) {
    
    }
    
    /**
     * Replies to command sender with a caption message
     *
     * @param prefix  the prefix before the caption message
     * @param caption the caption
     * @param context the context
     */
    @Override
    public void reply(String prefix, Caption<TestSender> caption, Context<TestSender> context) {
    
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
