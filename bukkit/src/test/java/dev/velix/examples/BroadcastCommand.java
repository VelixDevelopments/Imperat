package dev.velix.examples;

import dev.velix.BukkitSource;
import dev.velix.annotations.*;
import org.bukkit.Bukkit;

@Command("broadcast")
@Permission("command.broadcast")
@Description("A broadcast command")
public final class BroadcastCommand {
    
    
    //usage without args
    @Usage
    public void example(BukkitSource source) {
        source.reply("/broadcast <message...>");
    }
    
    
    @Usage
    @Description("broadcasts a message")
    public void example(BukkitSource source, @Named("message") @Greedy String msg) {
        // /broadcast <msg...>
        for (var player : Bukkit.getOnlinePlayers())
            player.sendMessage(msg);
        
        source.reply("Broadcast has been sent !");
    }
    
}
