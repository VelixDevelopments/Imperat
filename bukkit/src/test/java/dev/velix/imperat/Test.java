package dev.velix.imperat;


import dev.velix.imperat.annotations.Description;
import dev.velix.imperat.annotations.Permission;
import dev.velix.imperat.annotations.base.AnnotationFactory;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.examples.BanCommand;
import dev.velix.imperat.examples.ExampleCommand;
import dev.velix.imperat.examples.GroupCommand;
import dev.velix.imperat.examples.GuildCommand;
import dev.velix.imperat.examples.custom_annotations.MyCommand;
import dev.velix.imperat.examples.help.ExampleHelpTemplate;
import dev.velix.imperat.exception.ExecutionError;
import dev.velix.imperat.test.Group;
import dev.velix.imperat.test.GroupRegistry;
import dev.velix.imperat.test.GroupSuggestionResolver;
import dev.velix.imperat.test.guild.Guild;
import dev.velix.imperat.test.guild.GuildContextResolver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

@SuppressWarnings("unused")
public final class Test extends JavaPlugin implements Listener {
    
    private BukkitImperat dispatcher;
    
    @Override
    public void onEnable() {
        //testBukkit();
        this.getServer().getPluginManager().registerEvents(this, this);
        testImperat();
    }
    
    
    private void testImperat() {
        dispatcher = BukkitImperat.create(this);
        //testBrigadierCommodore();
        
        
        dispatcher.setHelpTemplate(new ExampleHelpTemplate());
        
        dispatcher.registerValueResolver(Group.class, ((source, context, raw, pivot, parameter) -> {
            Group group = GroupRegistry.getInstance().getData(raw).orElse(null);
            if (group == null)
                throw new ExecutionError("Invalid group '" + raw + "'");
            return group;
        }));
        
        dispatcher.registerSuggestionResolver(new GroupSuggestionResolver());
        //dispatcher.registerCommand(new BroadcastCommand());
        
        /*dispatcher.registerContextResolver(Group.class, (context, param) -> {
            var sender = context.getSource();
            if (sender.isConsole()) {
                throw new SenderErrorException("You don't have a guild !");
            }
            return GroupRegistry.getInstance()
                    .getGroup(sender.as(Player.class).getUniqueId());
        });*/
        
        
        dispatcher.registerAnnotationReplacer(MyCommand.class, (annotation) -> {
            var cmd = AnnotationFactory.create(
                    dev.velix.imperat.annotations.Command.class,
                    "value", new String[]{"name", "alias"});
            var permission = AnnotationFactory.create(Permission.class, "value", "command.group");
            var desc = AnnotationFactory.create(Description.class, "value",
                    "Main command for managing groups/ranks");
            
            return List.of(cmd, permission, desc);
        });
        
        dispatcher.registerContextResolver(Guild.class, new GuildContextResolver());
        
        //dispatcher.applyBrigadier();
        
        dispatcher.registerCommand(new ExampleCommand());
        //classicExample();
        dispatcher.registerCommand(new GuildCommand());
        dispatcher.registerCommand(new GroupCommand());
        dispatcher.registerCommand(new BanCommand());
        
        debugCommands();
    }
    
    private void classicExample() {
        
        Command<BukkitSource> example = Command.<BukkitSource>create("example")
                .usage(CommandUsage.<BukkitSource>builder()
                        .parameters(CommandParameter.requiredInt("firstArg"))
                        .execute((source, context) -> {
                            int firstArg = context.getArgumentOr("firstArg", -1);
                            source.reply("Entered value= " + firstArg);
                        })
                
                ).build();
        
        dispatcher.registerCommand(example);
    }
    
    private void classicBanExample() {
        final String defaultReason = "Breaking Server Laws";
        
        Command<BukkitSource> command = BukkitCommand.create("ban")
                .permission("command.ban")
                .description("Main command for banning players")
                .defaultExecution((source, context) -> {
                    source.reply("/ban <player> [-silent] [duration] [reason...]");
                })
                .usage(
                        BukkitUsage.builder()
                                .parameters(
                                        CommandParameter.required("player", OfflinePlayer.class),
                                        CommandParameter.flagSwitch("silent").aliases("-s"),
                                        CommandParameter.optionalText("duration"),
                                        CommandParameter.optionalGreedy("reason").defaultValue(defaultReason)
                                )
                                .execute((source, context) -> {
                                    OfflinePlayer player = context.getArgument("player");
                                    Boolean silent = context.getFlagValue("silent");
                                    if (silent == null)
                                        silent = false; //not needed but in case some bug happens out of nowhere
                                    
                                    String duration = context.getArgument("duration");
                                    String reason = context.getArgument("reason");
                                    
                                    String durationFormat = duration == null ? "FOREVER" : "for " + duration;
                                    assert player != null;
                                    String msg = "Banning " + player.getName() + " "
                                            + durationFormat + " due to " + reason;
                                    
                                    if (!silent)
                                        Bukkit.broadcastMessage(msg);
                                    else
                                        source.reply(msg);
                                    
                                })
                
                )
                .build();
        
    }
    
    
    private void classicGroupCmd() {
        
        Command<BukkitSource> senderCommand = BukkitCommand.create("group")
                .defaultExecution((source, context) -> {
                    source.reply("/group <group>");
                })
                .usage(
                        BukkitUsage.builder()
                                .parameters(CommandParameter.required("group", Group.class))
                                .execute((source, context) -> {
                                    Group group = context.getArgument("group");
                                    assert group != null;
                                    source.reply("entered group name= " + group.name());
                                })
                )
                .build();
        
    }
    
    private void classicGuildCmd() {
        Command<BukkitSource> guildCmd = BukkitCommand.create("guild")
                .subCommand(
                        BukkitCommand.create("disband")
                                .usage(
                                        BukkitUsage.builder()
                                                .execute((source, context) -> {
                                                    //getting our context resolved Guild object's instance
                                                    Guild guild = context.getContextResolvedArgument(Guild.class);
                                                    if (guild == null) {
                                                        //user has no guild
                                                        //do something,
                                                        // or you can process it
                                                        // to do something in the ContextResolver by making use of custom exceptions
                                                        return;
                                                    }
                                                    guild.disband();
                                                    source.reply("You have disbanded your guild successfully !!");
                                                })
                                ).build()
                )
                .build();
        
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        GroupRegistry.getInstance().setGroup(player.getUniqueId(), new Group("owner"));
    }
    
    
    private void debugCommands() {
        for (var command : dispatcher.getRegisteredCommands()) {
            System.out.println("Command '" + command.name() + "' has usages: ");
            for (CommandUsage<BukkitSource> usage : command.getUsages()) {
                System.out.println("- " + CommandUsage.format(command, usage));
            }
        }
    }
    
}
