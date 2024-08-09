package dev.velix.imperat;

import dev.velix.imperat.command.AbstractCommandDispatcher;
import dev.velix.imperat.resolvers.PermissionResolver;

public final class TestCommandDispatcher extends AbstractCommandDispatcher<TestSender> {

	TestCommandDispatcher() {
		super();
	}

	@Override
	public String commandPrefix() {
		return "";
	}

	/**
	 * @return {@link PermissionResolver} for the dispatcher
	 */
	@Override
	public PermissionResolver<TestSender> getPermissionResolver() {
		return null;
	}

	/**
	 * Wraps the sender into a built-in command-sender type
	 *
	 * @param sender the sender's actual value
	 * @return the wrapped command-sender type
	 */
	@Override
	public CommandSource<TestSender> wrapSender(TestSender sender) {
		return new TestCommandSource(sender);
	}

	/**
	 * @return the platform of the module
	 */
	@Override
	public Object getPlatform() {
		return null;
	}
}
