package dev.velix.examples.help;

import dev.velix.command.Command;
import dev.velix.help.HelpTemplate;
import dev.velix.help.UsageDisplayer;
import dev.velix.help.UsageFormatter;
import dev.velix.help.templates.DefaultFormatter;


public class ExampleHelpTemplate implements HelpTemplate {
    
    private UsageDisplayer displayer = UsageDisplayer.plain();
    private UsageFormatter formatter = new DefaultFormatter();
    
    @Override
    public String getHeader(Command<?> command) {
        return
                "&8&l&m===================&r &6"
                        + command.name() + "'s help&r &8&l&m===================";
    }
    
    
    @Override
    public String getFooter(Command<?> command) {
        return "&8&l&m=================================";
    }
    
    @Override
    public UsageFormatter getUsageFormatter() {
        return formatter;
    }
    
    @Override
    public void setUsageFormatter(UsageFormatter formatter) {
        this.formatter = formatter;
    }
    
    @Override
    public UsageDisplayer getUsagesDisplayer() {
        return displayer;
    }
    
    @Override
    public void setUsageDisplayer(UsageDisplayer displayer) {
        this.displayer = displayer;
    }
    
}
