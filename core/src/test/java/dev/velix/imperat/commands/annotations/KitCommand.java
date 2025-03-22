package dev.velix.imperat.commands.annotations;

import dev.velix.imperat.TestSource;
import dev.velix.imperat.annotations.Command;
import dev.velix.imperat.annotations.Default;
import dev.velix.imperat.annotations.Named;
import dev.velix.imperat.annotations.Optional;
import dev.velix.imperat.annotations.SubCommand;
import dev.velix.imperat.util.ImperatDebugger;

@Command("kit")
public final class KitCommand {

    @SubCommand(
        "create"
    )
    public void createKit(TestSource source, @Named("kit") String kit, @Named("weight") @Optional @Default("1") Integer weight) {
        ImperatDebugger.debug("kit=%s, weight=%s", kit, weight);
    }
}
