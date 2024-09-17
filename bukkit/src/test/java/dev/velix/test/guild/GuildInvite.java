package dev.velix.test.guild;

import org.bukkit.entity.Player;

public record GuildInvite(Player from, Player to, Guild guild) {

}
