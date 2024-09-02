package dev.velix.imperat;


import dev.velix.imperat.annotations.AnnotationFactory;
import dev.velix.imperat.annotations.types.Description;
import dev.velix.imperat.annotations.types.Permission;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.examples.BanCommand;
import dev.velix.imperat.examples.ExampleCommand;
import dev.velix.imperat.examples.GroupCommand;
import dev.velix.imperat.examples.GuildCommand;
import dev.velix.imperat.examples.custom_annotations.MyCommand;
import dev.velix.imperat.examples.help.ExampleHelpTemplate;
import dev.velix.imperat.exceptions.context.ContextResolveException;
import dev.velix.imperat.supplier.OptionalValueSupplier;
import dev.velix.imperat.test.Group;
import dev.velix.imperat.test.GroupRegistry;
import dev.velix.imperat.test.GroupSuggestionResolver;
import dev.velix.imperat.test.guild.Guild;
import dev.velix.imperat.test.guild.GuildContextResolver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
public final class Test extends JavaPlugin implements Listener {

    private BukkitImperat dispatcher;

    @Override
    public void onEnable() {
        //testBukkit();
        this.getServer().getPluginManager().registerEvents(this, this);
        testImperat();
    }

    private void testBuilderImperat() {
        var command = Command.createCommand("example");
        command.addAliases("example2", "example3", "example4", "example5");
        command.setPermission("command.example.permission");
        command.setDescription("This is an example command !");

        command.setDefaultUsageExecution((source, context) -> {
            source.reply("This is just an example with no arguments entered");
        });
        command.addUsage(CommandUsage.builder()
                .parameters(
                        CommandParameter.requiredInt("firstArg")
                )
                .execute((source, context) -> {
                    Integer firstArg = context.getArgument("firstArg");
                    source.reply("Entered required number= " + firstArg);
                })
                .build()
        );

        command.addSubCommandUsage("sub1", CommandUsage.builder()
                .parameters(
                        CommandParameter.optionalDouble("value")
                        .defaultValue(OptionalValueSupplier.of(-1D))
                )
                .execute((source, context) -> {

                    //you can get previously used arguments from the main command usage
                    Integer firstArg = context.getArgument("firstArg");
                    source.reply("Entered firstArg= " + firstArg);

                    Double value = context.getArgument("value");
                    assert value != null; //optional arg cant be null, it has a default value supplier
                    source.reply("Double value entered= " + value);
                })
                .build());
    }

    private void testImperat() {
        dispatcher = BukkitImperat.create(this);
        //testBrigadierCommodore();


        dispatcher.setHelpTemplate(new ExampleHelpTemplate());

        dispatcher.registerValueResolver(Group.class, ((source, context, raw, pivot, parameter) -> {
            Optional<Group> container = GroupRegistry.getInstance().getData(raw);
            return container.orElseThrow(() ->
                    new ContextResolveException("Invalid group '" + raw + "'"));
        }));

        dispatcher.registerSuggestionResolver(new GroupSuggestionResolver());
        //dispatcher.registerCommand(new BroadcastCommand());
		/*dispatcher.registerContextResolver(Group.class, (context, param)-> {
			var sender = context.getSource();
			if(sender.isConsole()) {
				return null;
			}
			return GroupRegistry.getInstance()
					  .getGroup(sender.as(Player.class).getUniqueId());
		});*/
		
		
        dispatcher.registerAnnotationReplacer(MyCommand.class, (annotation)-> {
          var cmd = AnnotationFactory.create(
                  dev.velix.imperat.annotations.types.Command.class,
                  "value" , new String[]{"name", "alias"});
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

        Command<CommandSender> example = Command.createCommand("example");
        example.addUsage(CommandUsage.<CommandSender>builder()
                .parameters(CommandParameter.requiredInt("firstArg"))
                .execute((source, context) -> {
                    int firstArg = context.getArgumentOr("firstArg", -1);
                    source.reply("Entered arg= " + firstArg);
                })
                .build());

        dispatcher.registerCommand(example);
    }

    private void classicBanExample() {
      Command<CommandSender> banCommand = Command.createCommand("ban");
      banCommand.setPermission("command.ban");
      banCommand.setDescription("Main command for banning players");
      banCommand.setDefaultUsageExecution((source, context)-> {
        source.reply("/ban <player> [-silent] [duration] [reason...]");
      });
      
      final String defaultReason = "Breaking Server Laws";
      banCommand.addUsage(
              CommandUsage.<CommandSender>builder()
                      .parameters(
                              CommandParameter.required("player", OfflinePlayer.class),
                              CommandParameter.flagSwitch("silent").aliases("-s"),
                              CommandParameter.optionalText("duration"),
                              CommandParameter.optionalGreedy("reason").defaultValue(defaultReason)
                      )
                      .execute((source, context)-> {
                        OfflinePlayer player = context.getArgument("player");
                        Boolean silent = context.getFlagValue("silent");
                        if(silent == null) silent = false; //not needed but in case some bug happens out of nowhere
                        
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
                      .build()
      );
      
    }
    

    private void classicGroupCmd() {

        Command<CommandSender> senderCommand = Command.createCommand("group");
        senderCommand.setDefaultUsageExecution((source, context) -> {
            source.reply("/group <group>");
        });

        senderCommand.addUsage(
                CommandUsage.<CommandSender>builder()
                        .parameters(CommandParameter.required("group", Group.class))
                        .execute((source, context) -> {
                            Group group = context.getArgument("group");
                            assert group != null;
                            source.reply("entered group name= " + group.name());
                        }).build());

    }

    private void classicGuildCmd() {
        Command<CommandSender> guildCmd = Command.createCommand("guild");
        guildCmd.addSubCommandUsage("disband", CommandUsage.<CommandSender>builder()
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
                .build()
        );
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        GroupRegistry.getInstance().setGroup(player.getUniqueId(), new Group("owner"));
    }


    private void debugCommands() {
        for (var command : dispatcher.getRegisteredCommands()) {
            System.out.println("Command '" + command.getName() + "' has usages: ");
            for (CommandUsage<CommandSender> usage : command.getUsages()) {
                System.out.println("- " + CommandUsage.format(command, usage));
            }
        }
    }

}
