package dev.velix.imperat.util.api;


import java.lang.annotation.*;

/**
 * Marks the class or method as unstable OR
 * may be buggy OR can be removed anytime with no
 * back-ward compatibility
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Unstable {


}
