package dev.velix.imperat;

import dev.velix.imperat.help.HelpTemplate;
import org.jetbrains.annotations.NotNull;

public sealed interface CommandHelpHandler permits Imperat {
    
    
    /**
     * @return The template for showing help
     */
    @NotNull
    HelpTemplate getHelpTemplate();
    
    /**
     * Set the help template to use
     *
     * @param template the help template
     */
    void setHelpTemplate(HelpTemplate template);
    
    
}
