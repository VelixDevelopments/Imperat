package dev.velix.imperat.commands;

import dev.velix.imperat.annotations.Command;
import dev.velix.imperat.annotations.Default;
import dev.velix.imperat.annotations.Description;
import dev.velix.imperat.annotations.Flag;
import dev.velix.imperat.annotations.Named;
import dev.velix.imperat.annotations.SubCommand;
import dev.velix.imperat.annotations.Switch;
import dev.velix.imperat.components.TestSource;

@Command("rank")
public class RankCommand {

    @SubCommand("addperm")
    @Description("Adds a permission")
    public void addPerm(
            final TestSource actor,
            @Named("rank") final String rank,
            @Named("permission") String permission,
            @Flag("duration") @Default("permanent") Duration duration,
            @Switch("force") final boolean force
    ) {
        actor.reply("rank= " + rank);
        actor.reply("perm= " + permission);
        actor.reply("duration= " + duration.toString());
        actor.reply("forced= " + force);
    }

}
