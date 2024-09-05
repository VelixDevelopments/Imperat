package dev.velix.imperat.examples.help;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.help.HelpTemplate;
import dev.velix.imperat.help.UsageFormatter;
import dev.velix.imperat.help.UsageDisplayer;
import dev.velix.imperat.help.templates.DefaultFormatter;


public class ExampleHelpTemplate implements HelpTemplate {
    
    private UsageDisplayer displayer = UsageDisplayer.plain();
    private UsageFormatter formatter = new DefaultFormatter();
    
    @Override
    public String getHeader(Command<?> command) {
        return
                "<dark_gray><bold><strikethrough>=================== <gold>"
                        + command.getName() + "'s help</gold> ===================";
    }
    
    
    @Override
    public String getFooter(Command<?> command) {
        return "<dark_gray><bold><strikethrough>===============================================";
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
