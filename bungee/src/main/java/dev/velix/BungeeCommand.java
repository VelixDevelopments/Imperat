package dev.velix;

import dev.velix.command.Command;

public interface BungeeCommand {
    
    static Command.Builder<BungeeSource> create(String name) {
        return Command.create(name);
    }
    
}
