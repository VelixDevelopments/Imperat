package dev.velix.imperat.caption;

import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a caption key
 *
 * @see Caption
 */
@ApiStatus.AvailableSince("1.0.0")
public interface CaptionKey {
    
    
    CaptionKey NO_PERMISSION = () -> "no_permission";
    
    CaptionKey COOLDOWN = () -> "wait_cooldown";
    
    CaptionKey INVALID_SYNTAX = () -> "invalid_syntax";
    
    CaptionKey NO_HELP_AVAILABLE_CAPTION = () -> "no_help_available";
    CaptionKey NO_HELP_PAGE_AVAILABLE_CAPTION = () -> "no_help_page_available";
    
    /**
     * the id of the caption
     *
     * @return {@link Caption}'s id
     */
    String id();
    
    
}
