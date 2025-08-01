package dev.velix.imperat.commands;

import dev.velix.imperat.annotations.*;
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
