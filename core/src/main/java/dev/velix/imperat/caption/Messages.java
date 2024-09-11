package dev.velix.imperat.caption;

import org.jetbrains.annotations.ApiStatus;

/**
 * Represents the interface that holds to all messages
 */
@ApiStatus.AvailableSince("1.0.0")
public interface Messages {
    
    String NO_PERMISSION = "<red> You don't have permission to do that !";
    String COOL_DOWN_WAIT = "<red> Please wait for <time> to execute that again !";
    String INVALID_SYNTAX_UNKNOWN_USAGE = "<yellow> Unknown command usage '<blue><raw_args></blue>' is unknown !";
    String INVALID_SYNTAX_INCOMPLETE_USAGE = "<gray> Missing required arguments '<red><required_args></red>'";
    String INVALID_SYNTAX_ORIGINAL_USAGE_SHOWCASE = "<white>'<yellow><usage></yellow>'";
    String NO_HELP_AVAILABLE = "<red> No Help available for <yellow>'<command>'";
    String NO_HELP_PAGE_AVAILABLE = "<red> Page '<page>' doesn't exist !";
    
}
