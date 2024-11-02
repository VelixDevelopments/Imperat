package dev.velix.imperat;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.PrintStream;

public final class CommandLineConfigBuilder extends ConfigBuilder<ConsoleSource, CommandLineImperat> {

    private final InputStream inputStream;

    private CommandLineConfigBuilder(InputStream inputStream) {
        this.inputStream = inputStream;
        config.registerSourceResolver(PrintStream.class, ConsoleSource::origin);
    }

    /**
     * Creates a new CommandLineConfigBuilder instance.
     *
     * @param inputStream the input stream for command line input
     * @return a new CommandLineConfigBuilder instance
     */
    public static CommandLineConfigBuilder builder(InputStream inputStream) {
        return new CommandLineConfigBuilder(inputStream);
    }

    /**
     * Builds and returns a configured CommandLineImperat instance.
     *
     * @return the CommandLineImperat instance
     */
    @Override
    public @NotNull CommandLineImperat build() {
        return new CommandLineImperat(inputStream, this.config);
    }
}