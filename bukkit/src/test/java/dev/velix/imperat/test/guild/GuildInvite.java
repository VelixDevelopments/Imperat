package dev.velix.imperat.test.guild;

import org.bukkit.entity.Player;

public record GuildInvite(Player from, Player to, Guild guild) {

}
