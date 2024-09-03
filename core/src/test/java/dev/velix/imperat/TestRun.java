package dev.velix.imperat;

import com.google.common.reflect.TypeToken;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.command.tree.CommandTree;
import dev.velix.imperat.command.tree.CommandTreeVisualizer;
import dev.velix.imperat.command.tree.Traverse;
import dev.velix.imperat.util.CommandDebugger;
import dev.velix.imperat.verification.UsageVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

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
 
	@Test
	public void testAmbiguity() {
		
		UsageVerifier<TestSender> verifier = UsageVerifier.typeTolerantVerifier();
		
		TypeToken<List<String>> ref = new TypeToken<>() {};
		System.out.println("REF= " + ref.getType().getTypeName());
		CommandUsage<TestSender> usage1 = CommandUsage.<TestSender>builder()
						.parameters(
										CommandParameter.requiredText("arg1")
						).build();
		
		CommandUsage<TestSender> usage2 = CommandUsage.<TestSender>builder()
						.parameters(
										CommandParameter.requiredBoolean("arg2")
						).build();
		
		Assertions.assertFalse(verifier.areAmbiguous(usage1, usage2));
	}
	
}
