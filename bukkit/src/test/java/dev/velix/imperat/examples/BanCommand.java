package dev.velix.imperat.examples;

import dev.velix.imperat.BukkitCommandSource;
import dev.velix.imperat.annotations.types.*;
import dev.velix.imperat.annotations.types.methods.DefaultUsage;
import dev.velix.imperat.annotations.types.methods.Usage;
import dev.velix.imperat.annotations.types.parameters.*;
import dev.velix.imperat.supplier.defaults.BooleanValueSupplier;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

@Command("ban")
@Permission("command.ban")
@Description("Main command for banning players")
public final class BanCommand {

    @DefaultUsage
    public void showUsage(BukkitCommandSource source) {
        source.reply("/ban <player> [-s] [duration] [reason...]");
    }

    @Usage
    public void banPlayer(
            BukkitCommandSource source,
            @Named("player") OfflinePlayer player,
            @Flag(value = "ip", inputType = Boolean.class) @DefaultValueProvider(BooleanValueSupplier.class) boolean silent,
            @Named("duration") @Optional @Nullable String duration,
            @Named("reason") @Optional @DefaultValue("Breaking server laws") @Greedy String reason
    ) {
        System.out.println("IP= " + silent);
        //TODO actual ban logic
        String durationFormat = duration == null ? "FOREVER" : "for " + duration;
        String msg = "Banning " + player.getName() + " " + durationFormat + " due to " + reason;
        if (!silent)
            Bukkit.broadcastMessage(msg);
        else
            source.reply(msg);
    }

}
