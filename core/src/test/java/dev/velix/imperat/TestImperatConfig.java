package dev.velix.imperat;

import org.jetbrains.annotations.*;

public final class TestImperatConfig extends ConfigBuilder<TestSource, TestImperat> {

    private TestImperatConfig() {

    }

    @Override
    public @NotNull TestImperat build() {
        return new TestImperat(config);
    }

    public static TestImperatConfig builder() {
        return new TestImperatConfig();
    }


}
