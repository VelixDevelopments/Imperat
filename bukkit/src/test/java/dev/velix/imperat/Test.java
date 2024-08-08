package dev.velix.imperat;


import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.UsageParameter;
import dev.velix.imperat.examples.GroupCommand;
import dev.velix.imperat.examples.help.ExampleHelpTemplate;
import dev.velix.imperat.exceptions.context.ContextResolveException;
import dev.velix.imperat.test.Group;
import dev.velix.imperat.test.GroupRegistry;
import dev.velix.imperat.test.GroupSuggestionResolver;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public final class Test extends JavaPlugin {

	private BukkitCommandDispatcher dispatcher;

	@Override
	public void onEnable() {
		//testBukkit();

		testImperat();
	}

	private void testBukkit() {
		BukkitTestCommand command = new BukkitTestCommand();

		PluginCommand bukkitCmd = this.getCommand("group");
		bukkitCmd.setExecutor(command);
		bukkitCmd.setTabCompleter(command);
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
		dispatcher.registerCommand(new GroupCommand());

		for (Command<CommandSender> command : dispatcher.getRegisteredCommands()) {
			System.out.println("Command ' " + command.getName() + "' has usages: ");
			for (CommandUsage<CommandSender> usage : command.getUsages()) {
				System.out.println("- " + CommandUsage.format(command, usage));
			}
		}
	}

	private void ot2() {

		Command<CommandSender> command = Command.createCommand("ot2");
		command.addFlag("silent", "s");

		command.addSubCommandUsage("testsub", CommandUsage.<CommandSender>builder()
				  .cooldown(1, TimeUnit.MINUTES)
				  .parameters(
							 UsageParameter.required("r1", String.class),
							 UsageParameter.flag("silent"),
							 UsageParameter.optional("o1", String.class, "idk"),
							 UsageParameter.required("r2", String.class),
							 UsageParameter.greedy("o2", true, "OPTIONAL GREEDY DEFAULT")
				  )
				  .execute(((commandSource, context) -> {
					  System.out.println("Running test-sub-1");

					  String r1 = context.getArgument("r1");
					  String o1 = context.getArgument("o1");
					  String r2 = context.getArgument("r2");
					  String o2 = context.getArgument("o2");

					  Boolean silent = context.getFlag("silent");

					  commandSource.reply(String.format("r1=%s, silent=%s, o1=%s, r2=%s, o2=%s", r1, silent,
								 o1, r2, o2));
				  }))
				  .build(), true);

		command.addSubCommandUsage("testsub2", CommandUsage.<CommandSender>builder()
				  .cooldown(15, TimeUnit.SECONDS)
				  .parameters(
							 UsageParameter.required("r1", String.class),
							 UsageParameter.flag("silent"),
							 UsageParameter.optional("o1", String.class, "idk"),
							 UsageParameter.required("r2", String.class),
							 UsageParameter.greedy("o2", true, "OPTIONAL GREEDY DEFAULT")
				  )
				  .execute(((commandSource, context) -> {
					  System.out.println("Running test-sub-2");
					  String r1 = context.getArgument("r1");
					  String o1 = context.getArgument("o1");
					  String r2 = context.getArgument("r2");
					  String o2 = context.getArgument("o2");

					  Boolean silent = context.getFlag("silent");

					  commandSource.reply(String.format("r1=%s, silent=%s, o1=%s, r2=%s, o2=%s", r1, silent,
								 o1, r2, o2));
				  }))
				  .build(), true);
		dispatcher.registerCommand(command);

	}

	private void ot1() {

		Command<CommandSender> command = Command.createCommand("ot");
		command.addUsage(CommandUsage.<CommandSender>builder()
				  .parameters(
							 UsageParameter.required("r1", String.class),
							 UsageParameter.optional("o1", Boolean.class, "false")
				  )
				  .execute(((commandSource, context) -> {

					  String r1 = context.getArgument("r1");
					  Boolean o1 = context.getArgument("o1");

					  commandSource.reply("r1= " + r1 + ", o1= " + o1);
				  }))
				  .build());

		dispatcher.registerCommand(command);

	}

	private void addGroupsCommand() {


		Command<CommandSender> command = Command.createCommand("group");
		command.setDefaultUsageExecution((source, context) -> {
			source.reply(ChatColor.RED + "/group help");
		});

		command.addUsage(CommandUsage.<CommandSender>builder()
				  .parameters(UsageParameter.required("group", Group.class)).build()
		);

		command.addSubCommandUsage("create",
				  CommandUsage.<CommandSender>builder()
							 .parameters(
										UsageParameter.required("name", String.class)
							 )
							 .execute((source, context) -> {
								 source.reply("Created group: " + context.getArgument("name"));
							 })
							 .build(),
				  true);

		command.addSubCommandUsage("setperm",
				  CommandUsage.<CommandSender>builder()
							 .parameters(
										UsageParameter.required("permission", String.class)
							 )
							 .execute((source, context) -> {
								 Group group = context.getArgument("group");
								 String permission = context.getArgument("permission");

								 source.reply("Added permission '" + permission + "' to group '" + group + "'");
							 })
							 .build()
		);

		command.addSubCommandUsage("setprefix",
				  CommandUsage.<CommandSender>builder()
							 .parameters(
										UsageParameter.required("prefix", String.class)
							 )
							 .execute((source, context) -> {
								 Group group = context.getArgument("group");
								 String prefix = context.getArgument("prefix");

								 source.reply("set prefix '" + prefix + "' to group '" + group + "'");
							 })
							 .build()
		);

		dispatcher.registerCommand(command);
	}

}
