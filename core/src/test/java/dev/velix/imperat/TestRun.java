package dev.velix.imperat;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.tree.CommandTree;
import dev.velix.imperat.tree.CommandTreeVisualizer;
import dev.velix.imperat.tree.Traverse;
import dev.velix.imperat.util.CommandDebugger;
import org.junit.jupiter.api.Test;

public class TestRun {

	TestRun() {
	}
	
	private Command<TestSender> groupCommand() {
		Command<TestSender> command = Command.createCommand("test");
		command.addUsage(CommandUsage.<TestSender>builder()
						.parameters(CommandParameter.requiredText("group")).build());
		
		command.addSubCommandUsage(
						"setperm", CommandUsage.<TestSender>builder()
										.parameters(
														CommandParameter.requiredText("permission"),
														CommandParameter.optionalBoolean("value").defaultValue(false)
										).build()
		);
		
		command.addSubCommandUsage(
						"setprefix", CommandUsage.<TestSender>builder()
										.parameters(
														CommandParameter.requiredText("prefix")
										).build()
		);
		
		command.addSubCommandUsage("help",
						CommandUsage.<TestSender>builder()
										.parameters(
														CommandParameter.optionalInt("page").defaultValue(1)
										)
										.build(),
						true);
		
		return command;
	}
	
	private Command<TestSender> complexCommand() {
		Command<TestSender> cmd = Command.createCommand("ot");
		cmd.addUsage(CommandUsage.<TestSender>builder()
										.parameters(
														CommandParameter.requiredText("r1"),
														CommandParameter.optionalText("o1"),
														CommandParameter.requiredText("r2"),
														CommandParameter.optionalText("o2")
										)
						.build());
		return cmd;
	}
	
	@Test
	public void testTreeLook() {
		
		CommandTree<TestSender> tree = CommandTree.create(groupCommand());
		tree.parseCommandUsages();
		
		CommandDebugger.debug("Visualizing tree");
		CommandTreeVisualizer<TestSender> visualizer = CommandTreeVisualizer.of(tree);
		visualizer.visualize();
		
		// command= /test
		ArgumentQueue input = ArgumentQueue.parse("member setperm");
		CommandDebugger.debug("traversing...");
		
		CommandDebugger.debug("Visualizing traversing result");
		Traverse traverse = tree.traverse(input);
		traverse.visualize();
	}
 
	
}
