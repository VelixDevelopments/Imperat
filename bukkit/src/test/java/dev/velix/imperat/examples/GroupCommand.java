package dev.velix.imperat.examples;

import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.annotations.types.Command;
import dev.velix.imperat.annotations.types.Permission;
import dev.velix.imperat.annotations.types.SubCommand;
import dev.velix.imperat.annotations.types.Usage;
import dev.velix.imperat.annotations.types.Named;
import dev.velix.imperat.annotations.types.SuggestionProvider;
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
