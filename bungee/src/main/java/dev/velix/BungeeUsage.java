package dev.velix;

import dev.velix.command.CommandUsage;

public interface BungeeUsage {
    
    static CommandUsage.Builder<BungeeSource> builder() {
        return CommandUsage.builder();
    }
    
}
