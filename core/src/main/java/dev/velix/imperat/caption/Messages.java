package dev.velix.imperat.caption;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents the interface that holds to all messages
 * formatted in the form of {@link MiniMessage}
 */
@ApiStatus.AvailableSince("1.0.0")
public interface Messages {

    MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();


    String NO_PERMISSION = "<red> You don't have permission to do that !";

    String COOL_DOWN_WAIT = "<red> Please wait for <time> to execute that again !";
    //TODO add more

    String INVALID_SYNTAX_UNKNOWN_USAGE = "<yellow> Unknown command usage '<blue><raw_args></blue>' is unknown !";

    String INVALID_SYNTAX_INCOMPLETE_USAGE = "<gray> Missing required arguments '<red><required_args></red>'";

    String INVALID_SYNTAX_ORIGINAL_USAGE_SHOWCASE = "<white>'<yellow><usage></yellow>'";

    String NO_HELP_AVAILABLE = "<red> No Help available for <yellow>'<command>'";

    String NO_HELP_PAGE_AVAILABLE = "<red> Page '<page>' doesn't exist !";

    static Component getMsg(String message, TagResolver... tagResolvers) {
        return MINI_MESSAGE.deserialize(message, tagResolvers);
    }

}
