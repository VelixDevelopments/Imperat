package dev.velix.imperat.context.internal;

import dev.velix.imperat.context.FlagData;

public record CommandFlag(FlagData<?> flag, String flagRaw,
                          String flagRawInput, Object value) {

    public boolean isSwitch() {
        return flag.inputType() == null;
    }

}
