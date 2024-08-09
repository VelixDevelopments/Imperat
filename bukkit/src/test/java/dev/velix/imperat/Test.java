package dev.velix.imperat;


import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.examples.GroupCommand;
import dev.velix.imperat.examples.help.ExampleHelpTemplate;
import dev.velix.imperat.exceptions.context.ContextResolveException;
import dev.velix.imperat.test.Group;
import dev.velix.imperat.test.GroupRegistry;
import dev.velix.imperat.test.GroupSuggestionResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

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
		dispatcher.registerContextResolver(Group.class, (context, param)-> {
			var sender = context.getCommandSource();
			if(sender.isConsole()) {
				return null;
			}
			return GroupRegistry.getInstance()
					  .getGroup(sender.as(Player.class).getUniqueId());
		});

		dispatcher.registerCommand(new GroupCommand());
		debugCommands();
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		var player = event.getPlayer();
		GroupRegistry.getInstance().setGroup(player.getUniqueId(), new Group("owner"));
	}


	private void debugCommands() {
		for (Command<CommandSender> command : dispatcher.getRegisteredCommands()) {
			System.out.println("Command ' " + command.getName() + "' has usages: ");
			for (CommandUsage<CommandSender> usage : command.getUsages()) {
				System.out.println("- " + CommandUsage.format(command, usage));
			}
		}
	}

}
