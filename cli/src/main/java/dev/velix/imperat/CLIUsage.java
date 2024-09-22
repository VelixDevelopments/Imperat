package dev.velix.imperat;

import dev.velix.imperat.command.CommandUsage;

public interface CLIUsage {
    
    static CommandUsage.Builder<ConsoleSource> builder() {
        return CommandUsage.builder();
    }
    
    
}
