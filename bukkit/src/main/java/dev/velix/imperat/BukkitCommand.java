package dev.velix.imperat;

import dev.velix.imperat.command.Command;

public interface BukkitCommand {

    static Command.Builder<BukkitSource> create(String name) {
        return Command.create(name);
    }

}
