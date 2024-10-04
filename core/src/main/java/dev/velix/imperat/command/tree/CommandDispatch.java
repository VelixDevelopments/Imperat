package dev.velix.imperat.command.tree;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.util.ImperatDebugger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class CommandDispatch<S extends Source> implements Iterable<CommandParameter<S>> {
	
	private final List<CommandParameter<S>> parameters = new ArrayList<>();
	
	private Result result;
	
	private CommandDispatch(Result result) {
		this.result = result;
	}
	
	static <S extends Source> CommandDispatch<S> of(final Result result) {
		return new CommandDispatch<>(result);
	}
	
	static <S extends Source> CommandDispatch<S> empty() {
		return of(Result.UNKNOWN);
	}
	
	public void append(ParameterNode<S, ?> node) {
		if (node == null) return;
		if (parameters.contains(node.data)) return;
		parameters.add(node.data);
	}
	
	public CommandParameter<S> getLastParameter() {
		return parameters.get(parameters.size() - 1);
	}
	
	@Override
	public @NotNull Iterator<CommandParameter<S>> iterator() {
		return parameters.iterator();
	}
	
	public Result result() {
		return result;
	}
	
	public void result(Result result) {
		this.result = result;
	}
	
	public @Nullable CommandUsage<S> toUsage(Command<S> command) {
		return command.getUsage(parameters);
	}
	
	public void visualize() {
		ImperatDebugger.debug("Result => " + result.name());
		StringBuilder builder = new StringBuilder();
		for (var node : parameters) {
			builder.append(node.format()).append(" -> ");
		}
		ImperatDebugger.debug(builder.toString());
	}
	
	/**
	 * Defines a result from dispatching the command execution.
	 */
	public enum Result {
		
		/**
		 * Defines a complete dispatch of the command,
		 * {@link CommandUsage} cannot be null unless the {@link CommandTree} has issues
		 */
		COMPLETE,
		
		/**
		 * Defines an incomplete execution, due to incomplete usage arguments.
		 * May occur with command default execution(with no args). e.g: `/cmd`
		 */
		INCOMPLETE,
		
		/**
		 * Defines an unknown execution/command, it's the default result
		 */
		UNKNOWN
		
	}
}
