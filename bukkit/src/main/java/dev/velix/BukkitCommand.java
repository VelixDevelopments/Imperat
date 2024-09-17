package dev.velix;

import dev.velix.command.Command;

public interface BukkitCommand {
    
    static Command.Builder<BukkitSource> create(String name) {
        return Command.create(name);
    }
    
}
