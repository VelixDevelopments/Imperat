package dev.velix.imperat.caption;

/**
 * Represents a caption key
 * @see Caption
 */
public interface CaptionKey {


	CaptionKey NO_PERMISSION = ()-> "no_permission";

	CaptionKey COOLDOWN = ()-> "wait_cooldown";

	CaptionKey INVALID_SYNTAX = ()-> "invalid_syntax";

	/**
	 * the id of the caption
	 * @return {@link Caption}'s id
	 */
	String id();


}
