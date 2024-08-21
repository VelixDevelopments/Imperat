package dev.velix.imperat;


import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.examples.BanCommand;
import dev.velix.imperat.examples.GroupCommand;
import dev.velix.imperat.examples.GuildCommand;
import dev.velix.imperat.examples.MyContextResolverFactory;
import dev.velix.imperat.examples.help.ExampleHelpTemplate;
import dev.velix.imperat.exceptions.context.ContextResolveException;
import dev.velix.imperat.test.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

@SuppressWarnings("unused")
public final class Test extends JavaPlugin implements Listener {
	
	private BukkitCommandDispatcher dispatcher;
	
	@Override
	public void onEnable() {
		//testBukkit();
		this.getServer().getPluginManager().registerEvents(this, this);
		testImperat();
	}
	
	private void testImperat() {
		dispatcher = BukkitCommandDispatcher.create(this);
		
		dispatcher.setHelpTemplate(new ExampleHelpTemplate());
		
		dispatcher.registerValueResolver(Group.class, ((source, context, raw) -> {
			Optional<Group> container = GroupRegistry.getInstance().getData(raw);
			return container.orElseThrow(() ->
							new ContextResolveException("Invalid group '" + raw + "'"));
		}));
		
		dispatcher.registerSuggestionResolver(new GroupSuggestionResolver());
		//dispatcher.registerCommand(new BroadcastCommand());
		/*dispatcher.registerContextResolver(Group.class, (context, param)-> {
			var sender = context.getCommandSource();
			if(sender.isConsole()) {
				return null;
			}
			return GroupRegistry.getInstance()
					  .getGroup(sender.as(Player.class).getUniqueId());
		});*/
		
		dispatcher.registerContextResolverFactory(new MyContextResolverFactory());
		
		dispatcher.registerContextResolver(Guild.class, (context, parameter) -> {
			var sender = context.getCommandSource();
			if (sender.isConsole()) {
				return null;
			}
			return GuildRegistry.getInstance()
							.getUserGuild(sender.as(Player.class).getUniqueId());
		});
		
		/*dispatcher.registerAnnotationReplacer(MyCustomAnnotation.class, (annotation)-> {
			var cmdAnn = AnnotationFactory.create(Command.class, "value" , new String[]{"group", "rank"});
			var permission = AnnotationFactory.create(Permission.class, "value", "command.group");
			var desc = AnnotationFactory.create(Description.class, "value",
							"Main command for managing groups/ranks");
			
			return List.of(cmdAnn, permission, desc);
		});*/
		//TODO test @Range
		dispatcher.registerCommand(new GuildCommand());
		dispatcher.registerCommand(new GroupCommand());
		dispatcher.registerCommand(new BanCommand());
		debugCommands();
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
