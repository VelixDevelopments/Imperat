package dev.velix.imperat;

import dev.velix.imperat.command.Command;

public interface BungeeCommand {

    static Command.Builder<BungeeSource> create(String name) {
        return Command.create(name);
    }

}
