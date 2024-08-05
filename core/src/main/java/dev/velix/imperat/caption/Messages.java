package dev.velix.imperat.caption;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public interface Messages {

	MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

	String NO_PERMISSION = "<red> You don't have permission to do that !";

	String COOL_DOWN_WAIT = "<red> Please wait for <time> to execute that again !";
	//TODO add more

	String INVALID_SYNTAX_UNKNOWN_USAGE = "<yellow> Unknown command usage '<blue><raw_args></blue>' is unknown !";

	String INVALID_SYNTAX_INCOMPLETE_USAGE = "<gray> Missing required arguments '<red><required_args></red>'";

	String INVALID_SYNTAX_ORIGINAL_USAGE_SHOWCASE = "<white>'<yellow><usage></yellow>'";

	static Component getMsg(String message, TagResolver... tagResolvers) {
		return MINI_MESSAGE.deserialize(message, tagResolvers);
	}

}
