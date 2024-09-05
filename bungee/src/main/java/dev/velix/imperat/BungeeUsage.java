package dev.velix.imperat;

import dev.velix.imperat.command.CommandUsage;

public interface BungeeUsage {

    static CommandUsage.Builder<BungeeSource> builder() {
        return CommandUsage.builder();
    }

}
