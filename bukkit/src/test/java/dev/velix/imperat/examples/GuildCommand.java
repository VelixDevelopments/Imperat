package dev.velix.imperat.examples;

import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.annotations.Command;
import dev.velix.imperat.annotations.Named;
import dev.velix.imperat.annotations.SubCommand;
import dev.velix.imperat.annotations.Usage;
import dev.velix.imperat.test.guild.Guild;
import dev.velix.imperat.test.guild.GuildInvite;
import dev.velix.imperat.test.guild.GuildRegistry;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Command("guild")
public class GuildCommand {
    
    private final Map<UUID, Set<GuildInvite>> invites = new HashMap<>();
    
    @Usage
    public void defaultUsage(BukkitSource source) {
        source.reply("No args input !");
    }
    
    @SubCommand("create")
    public void create(BukkitSource source, @Named("guild") String guildName) {
        Guild newGuild = new Guild(guildName);
        newGuild.addMember(source.asPlayer().getUniqueId());
        GuildRegistry.getInstance().registerGuild(newGuild);
        source.reply("You have created the guild '" + guildName + "'");
    }
    
    @SubCommand("disband")
    public void disband(BukkitSource source, Guild guild) {
        if (guild == null) {
            source.reply("HAHA YOU HAVE NO GUILD LLL");
            //user has no guild
            //do something,
            // or you can process it
            // to do something in the ContextResolver by making use of custom exceptions
            return;
        }
        guild.disband();
        source.reply("You have disbanded your guild successfully !!");
    }
    
    @SubCommand("invite")
    public void invite(
            @NotNull BukkitSource source,
            @Named("player") Player player,
            @NotNull Guild guild
    ) {
        Player sourcePlayer = source.asPlayer();
        guild.sendInvite(sourcePlayer, player);
        
        //adding the invite to the invites related to the receiver of the invite
        invites.compute(player.getUniqueId(), (uuid, oldInvites) -> {
            var guildInvite = new GuildInvite(sourcePlayer, player, guild);
            if (oldInvites == null) {
                Set<GuildInvite> invites = new HashSet<>();
                invites.add(guildInvite);
                return invites;
            }
            oldInvites.add(guildInvite);
            return oldInvites;
        });
        
        player.sendMessage("You have been invited to guild '"
                + guild.getName() + "' by " + sourcePlayer.getName());
    }
    
    @SubCommand("accept")
    public void accept(
            @NotNull BukkitSource source,
            @Nullable Guild sourceGuild,
            @Named("inviter") Player inviter
    ) {
        Player sourcePlayer = source.asPlayer();
        Set<GuildInvite> targetInvites = invites.get(sourcePlayer.getUniqueId());
        
        if (sourceGuild != null) {
            source.reply("You're already in a sourceGuild , you can't do that !");
            return;
        }
        
        GuildInvite invite = getInvite(targetInvites, inviter);
        
        if (targetInvites.isEmpty() || invite == null) {
            source.reply("no invites from '" + inviter.getName() + "'");
            return;
        }
        
        Guild targetGuild = invite.guild();
        targetGuild.addMember(sourcePlayer.getUniqueId());
        sourcePlayer.sendMessage("You have accepted " + inviter.getName() + "'s invite to his sourceGuild '" + targetGuild.getName() + "'");
    }
    
    private GuildInvite getInvite(Set<GuildInvite> invites, Player inviter) {
        for (var inv : invites) {
            if (inv.from().getUniqueId().equals(inviter.getUniqueId()))
                return inv;
        }
        return null;
    }
}
