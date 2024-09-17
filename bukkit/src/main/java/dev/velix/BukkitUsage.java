package dev.velix;

import dev.velix.command.CommandUsage;

public interface BukkitUsage {
    
    static CommandUsage.Builder<BukkitSource> builder() {
        return CommandUsage.builder();
    }
    
}
