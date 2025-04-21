package dev.velix.imperat.annotations;

import dev.velix.imperat.command.AttachmentMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface GlobalAttachmentMode {

    /**
     * Retrieves the {@link AttachmentMode} associated with this annotation.
     * @return the attachment mode that defines how a subcommand integrates with the usages of a command.
     */
    AttachmentMode value();

}
