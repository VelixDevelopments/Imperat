package dev.velix.imperat.context.internal;

import dev.velix.imperat.context.CommandFlag;
import dev.velix.imperat.context.CommandSwitch;

public record ResolvedFlag(CommandFlag flag, String flagRaw,
                           String flagRawInput, Object value) {

    public boolean isSwitch() {
        return flag instanceof CommandSwitch;
    }

}
