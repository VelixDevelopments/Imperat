package dev.velix.imperat.annotations.types.methods;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubCommand {

    String[] value();

    boolean ignoreAutoCompletionChecks() default false;
    
    boolean attachDirectly() default false;
}
