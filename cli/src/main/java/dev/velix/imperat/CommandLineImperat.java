package dev.velix.imperat;

import dev.velix.imperat.command.BaseImperat;
import dev.velix.imperat.command.tree.CommandDispatch;

import java.io.*;

public final class CommandLineImperat extends BaseImperat<ConsoleSource> {
    
    private InputStream input;
    
    private CommandLineImperat(InputStream inputStream) {
        super(((source, permission) -> true));
        this.input = inputStream;
    }
    
    public static CommandLineImperat create(InputStream inputStream) {
        return new CommandLineImperat(inputStream);
    }
    
    @Override
    public InputStream getPlatform() {
        return input;
    }
    
    @Override
    public void shutdownPlatform() {
        input = null;
        throw new RuntimeException();
    }
    
    @Override
    public String commandPrefix() {
        return "";
    }
    
    @Override
    public ConsoleSource wrapSender(Object sender) {
        if (!(sender instanceof PrintStream printStream)) {
            throw new UnsupportedOperationException("Sender must be a print stream");
        }
        return new ConsoleSource(printStream);
    }
    
    /**
     * Dispatches the command-line from the input stream provided
     *
     * @param outputStream the output stream/command-source origin
     * @return the usage match result
     */
    public CommandDispatch.Result dispatch(OutputStream outputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            String line = reader.readLine();
            ConsoleSource prompt = wrapSender(outputStream);
            return super.dispatch(prompt, line);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Dispatches the command-line from the input stream provided
     * while using {@link System#out} as an {@link OutputStream}
     *
     * @return the usage match result
     */
    public CommandDispatch.Result dispatch() {
        return dispatch(System.out);
    }
    
}
