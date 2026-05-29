package studio.mevera.imperat.annotations.types;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Restricts an {@code OfflinePlayer} argument to suggest only currently
 * online players during tab-completion, skipping the (potentially large)
 * offline player cache. The argument still accepts any known offline
 * player at parse time — only suggestions are affected.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface OnlineOnly {
}
