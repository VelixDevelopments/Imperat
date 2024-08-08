package dev.velix.imperat;

import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.UsageParameter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.lang.reflect.Field;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;

@Deprecated
public class LegacyCommandListener implements Listener {

	private final CommandDispatcher<CommandSender> dispatcher;

	public LegacyCommandListener(CommandDispatcher<CommandSender> dispatcher) {
		this.dispatcher = dispatcher;
	}


	@EventHandler
	public void onServerCommand(ServerCommandEvent event) {
		System.out.println("CMD=" + event.getCommand());
		String[] args = event.getCommand().split(" ");

		String[] rawArgs = new String[args.length - 1];
		System.arraycopy(args, 1, rawArgs, 0, args.length - 1);
		dispatcher.dispatch(event.getSender(), args[0], rawArgs);

		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onCommand(PlayerCommandPreprocessEvent event) {
		String message = event.getMessage();
		System.out.println("Message= " + message);
		event.setCancelled(true);
	}


	@EventHandler(priority = EventPriority.LOWEST)
	public void onPluginEnable(PluginEnableEvent event) {
		registerNewCommands();
	}


	private void registerNewCommands() {
		try {
			// Access the CommandMap
			Map<String, Command> commands = getCommandMap();

			for (Map.Entry<String, Command> entry : commands.entrySet()) {
				Command command = entry.getValue();
				dev.velix.imperat.command.Command<CommandSender> imperatCommand = dispatcher.getCommand(command.getName());
				if (imperatCommand != null) continue;
				dispatcher.registerCommand(bukkitCommandToImperat(command));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Command> getCommandMap() throws NoSuchFieldException, IllegalAccessException {
		CraftServer craftServer = (CraftServer) getServer();
		Field commandMapField = craftServer.getClass().getDeclaredField("commandMap");
		commandMapField.setAccessible(true);
		CommandMap commandMap = (CommandMap) commandMapField.get(craftServer);

		// Get the commands registered in the command map
		Field hashMapCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
		hashMapCommandsField.setAccessible(true);
		return (Map<String, Command>) hashMapCommandsField.get(commandMap);
	}

	public static dev.velix.imperat.command.Command<CommandSender> bukkitCommandToImperat(Command bukkitCommand) {

		dev.velix.imperat.command.Command<CommandSender> command =
				  dev.velix.imperat.command.Command.createCommand(bukkitCommand.getName());
		command.addAliases(bukkitCommand.getAliases());

		String usage = processUsage(bukkitCommand.getUsage());
		String[] splitUsage = usage.split(" ");

		UsageParameter[] parameters = new UsageParameter[splitUsage.length - 1];
		boolean collected = false;
		for (int i = 1; i < splitUsage.length; i++) {
			String arg = splitUsage[i];
			String argId = getArgName(arg);

			if (argId.contains("|")) {
				collected = true;
				String[] literals = argId.replace("|", " ").split(" ");
				for (String literal : literals) {
					UsageParameter literalParam = UsageParameter.required(literal, String.class);
					CommandUsage<CommandSender> commandUsage = CommandUsage.<CommandSender>builder()
							  .parameters(literalParam)
							  .execute((sender, context) ->
										 Bukkit.dispatchCommand(sender.getOrigin(), command.getName() + " " + literal))
							  .build();

					command.addUsage(commandUsage);
				}

				break;
			} else {
				parameters[i - 1] = isOptionalArg(arg)
						  ? UsageParameter.optional(argId, String.class, null)
						  : UsageParameter.required(argId, String.class);
			}
		}

		if (!collected) {
			command.addUsage(
					  CommandUsage.<CommandSender>builder()
								 .parameters(parameters)
								 .execute((source, context) ->
											bukkitCommand.execute(source.getOrigin(), context.getCommandUsed(),
													  context.getArguments().toArray(new String[0])))
								 .build()
			);
		}

		return command;
	}

	private static String getArgName(String arg) {
		StringBuilder builder = new StringBuilder();
		for (int i = 1; i < arg.length() - 1; i++) {
			builder.append(arg.charAt(i));
		}
		return builder.toString();
	}

	private static boolean isArgStarter(char c) {
		return c == '<' || c == '[';
	}

	private static boolean isArgEnder(char c) {
		return c == '>' || c == ']';
	}

	private static boolean isOptionalArg(String argName) {
		return argName.startsWith("[") && argName.endsWith("]");
	}

	private static boolean isRequiredArg(String argName) {
		return argName.startsWith("<") && argName.endsWith(">");
	}

	private static String processUsage(final String usage) {
		char[] chars = usage.toCharArray();
		for (int i = usage.indexOf(' ') + 1; i < chars.length; i++) {
			char c = chars[i];
			if (!isArgStarter(c)) {
				continue;
			}
			while (!isArgEnder(chars[i])) {
				if (Character.isWhitespace(chars[i])) {
					chars[i] = '-';
				}
				i++;
			}

		}
		return new String(chars);
	}

}
