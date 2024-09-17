package dev.velix.help.templates;

import dev.velix.command.Command;
import dev.velix.help.HelpTemplate;
import dev.velix.help.UsageDisplayer;
import dev.velix.help.UsageFormatter;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class DefaultTemplate implements HelpTemplate {
    
    
    private UsageFormatter formatter = new DefaultFormatter();
    private UsageDisplayer displayer = UsageDisplayer.plain();
    
    public DefaultTemplate() {
    }
    
    /**
     * @return the header
     */
    @Override
    public String getHeader(Command<?> command) {
        return "&8&l&m===================&r &2" + command.name() + "'s help&r &8&l&m===================";
    }
    
    /**
     * @return the footer
     */
    @Override
    public String getFooter(Command<?> command) {
        return "&8&l&m======================================";
    }
    
    /**
     * The usage formatter for the help-template
     *
     * @return the formatter of the usage
     */
    @Override
    public UsageFormatter getUsageFormatter() {
        return formatter;
    }
    
    /**
     * sets the usage help-formatter
     *
     * @param formatter the instance of the formatter to set to
     */
    @Override
    public void setUsageFormatter(UsageFormatter formatter) {
        this.formatter = formatter;
    }
    
    /**
     * @return the usage displayer
     */
    @Override
    public UsageDisplayer getUsagesDisplayer() {
        return displayer;
    }
    
    /**
     * Sets the usage displayer instance
     * to a new one
     *
     * @param displayer the displayer instance to set
     */
    @Override
    public void setUsageDisplayer(UsageDisplayer displayer) {
        this.displayer = displayer;
    }
    
    
}
