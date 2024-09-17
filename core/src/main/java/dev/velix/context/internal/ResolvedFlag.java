package dev.velix.context.internal;

import dev.velix.context.CommandFlag;
import dev.velix.context.CommandSwitch;

public record ResolvedFlag(CommandFlag flag, String flagRaw,
                           String flagRawInput, Object value) {
    
    public boolean isSwitch() {
        return flag instanceof CommandSwitch;
    }
    
}
