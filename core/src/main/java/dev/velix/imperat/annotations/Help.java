package dev.velix.imperat.annotations;

import dev.velix.imperat.help.HelpProvider;
import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ApiStatus.AvailableSince("1.9.0")
public @interface Help {

    /**
     * Specifies the class to be used as the help provider.
     *
     * @return a class extending {@code HelpProvider<?>} that defines the help functionality.
     */
    Class<? extends HelpProvider<?>> value();

}
