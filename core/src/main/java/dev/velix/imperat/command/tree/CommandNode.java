package dev.velix.imperat.command.tree;

import dev.velix.imperat.command.Command;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
final class CommandNode<C> extends UsageNode<Command<C>> {
	
	CommandNode(@NotNull Command<C> data) {
		super(data);
	}
	
	boolean isSubCommand() {
		return data.hasParent();
	}
	
	boolean isRoot() {
		return !isSubCommand();
	}
	
	@Override
	public boolean matchesInput(String raw) {
		return data.hasName(raw);
	}
	

	@Override
	public String format() {
		return data.format();
	}
	
	@Override
	public int priority() {
		return -1;
	}
	
}
