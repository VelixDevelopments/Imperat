package dev.velix.imperat.commands.annotations.examples;

import dev.velix.imperat.TestCommandHelp;
import dev.velix.imperat.TestSource;
import dev.velix.imperat.annotations.types.*;

@Command("group")
public final class AnnotatedGroupCommand {
    
    @Usage
    public void defaultUsage(TestSource source) {
        //default execution = no args
        source.reply("/group <group>");
    }
    
    @Usage
    public void mainUsage(
            TestSource source,
            @Named("group") @SuggestionProvider("groups") Group group
    ) {
        //when he does "/group <group>"
        source.reply("entered group name= " + group.name());
    }
    
    @SubCommand(value = "help", attachDirectly = true)
    public void help(TestSource source, TestCommandHelp help) {
        help.display(source);
        System.out.println("HELOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
    }
    
    @SubCommand("setperm")
    @Permission("command.group.setperm")
    public void setPermission(TestSource source,
                              @Named("group") Group group,
                              @Named("permission") String permission) {
        // /group <group> setperm <permission>
        source.reply("You have set permission '" + permission
                + "' to group '" + group.name() + "'");
    }
    
    @SubCommand("setprefix")
    @Permission("command.group.setprefix")
    public void setPrefix(
            TestSource source,
            @Named("group") Group group,
            @Named("prefix") String prefix
    ) {
        // /group <group> setprefix <prefix>
        source.reply("You have set prefix '" + prefix + "' to group '" + group.name() + "'");
    }
    
}
