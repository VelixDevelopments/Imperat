package dev.velix.imperat.components;

import dev.velix.imperat.ConfigBuilder;
import org.jetbrains.annotations.NotNull;

public final class TestImperatConfig extends ConfigBuilder<TestSource, TestImperat, TestImperatConfig> {

    public TestImperatConfig() {
        super();
    }

    @Override
    public @NotNull TestImperat build() {
        return new TestImperat(config);
    }

    public static TestImperatConfig builder() {
        return new TestImperatConfig();
    }


}
