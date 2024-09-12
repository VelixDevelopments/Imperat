package dev.velix.imperat.examples;

import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.annotations.types.*;
import dev.velix.imperat.help.CommandHelp;
import dev.velix.imperat.test.Group;

@Command("group")
public final class GroupCommand {
    
    @Usage
    public void defaultUsage(BukkitSource source) {
        //default execution = no args
        source.reply("/group <group>");
    }
    
    @Usage
    public void mainUsage(
            BukkitSource source,
            @Named("group") @SuggestionProvider("groups") Group group
    ) {
        //when he does "/group <group>"
        source.reply("entered group name= " + group.name());
    }
    
    @SubCommand(value = "help", attachDirectly = true)
    public void help(BukkitSource source, CommandHelp<BukkitSource> help) {
        //System.out.println("EXECUTING HELP, " + help);
        //source.reply("EXECUTING HELP !!");
        help.display(source);
    }
    
    @SubCommand(value = "setperm")
    @Permission("command.group.setperm")
    public void setPermission(BukkitSource source,
                              @Named("group") Group group,
                              @Named("permission") String permission) {
        // /group <group> setperm <permission>
        source.reply("You have set permission '" + permission
                + "' to group '" + group.name() + "'");
    }
    
    @SubCommand("setprefix")
    @Permission("command.group.setprefix")
    public void setPrefix(
            BukkitSource source,
            @Named("group") Group group,
            @Named("prefix") String prefix
    ) {
        // /group <group> setprefix <prefix>
        source.reply("You have set prefix '" + prefix + "' to group '" + group.name() + "'");
    }
    
}
