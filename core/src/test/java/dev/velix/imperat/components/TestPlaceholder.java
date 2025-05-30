package dev.velix.imperat.components;

import static dev.velix.imperat.TestRun.IMPERAT;

import dev.velix.imperat.placeholders.Placeholder;
import dev.velix.imperat.placeholders.PlaceholderRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestPlaceholder {
    PlaceholderRegistry<TestSource> placeholderRegistry = PlaceholderRegistry.createDefault(IMPERAT.config());

    public TestPlaceholder() {
        placeholderRegistry.setData("%description%", Placeholder.<TestSource>builder("%description%")
            .resolver(((placeHolderId, imperat) -> "My desc")).build());
    }

    @Test
    public void test1() {
        String str = "This is my desc: %description%";
        String value = placeholderRegistry.resolvedString(str);
        //System.out.println(value.replaceAll("%description%", "My desc"));
        Assertions.assertEquals("This is my desc: My desc", value);
    }

}
