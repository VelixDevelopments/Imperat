package dev.velix.imperat;

import dev.velix.imperat.command.CommandUsage;

public interface BukkitUsage {
    
    static CommandUsage.Builder<BukkitSource> builder() {
        return CommandUsage.builder();
    }
    
}
