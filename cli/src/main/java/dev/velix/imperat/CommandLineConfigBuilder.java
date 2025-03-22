package dev.velix.imperat;

import org.jetbrains.annotations.*;

import java.io.InputStream;
import java.io.PrintStream;

public final class CommandLineConfigBuilder extends ConfigBuilder<ConsoleSource, CommandLineImperat> {

    private final InputStream inputStream;

    CommandLineConfigBuilder(InputStream inputStream) {
        this.inputStream = inputStream;
        config.registerSourceResolver(PrintStream.class, ConsoleSource::origin);
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