package dev.velix.imperat.examples.help;

import dev.velix.imperat.caption.Messages;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.help.HelpTemplate;
import dev.velix.imperat.help.UsageFormatter;
import dev.velix.imperat.help.UsageDisplayer;
import dev.velix.imperat.help.templates.DefaultFormatter;
import net.kyori.adventure.text.Component;


public class ExampleHelpTemplate implements HelpTemplate {

    private UsageDisplayer displayer = UsageDisplayer.plain();
    private UsageFormatter formatter = new DefaultFormatter();

    @Override
    public Component getHeader(Command<?> command) {
        return Messages.getMsg(
                "<dark_gray><bold><strikethrough>=================== <gold>"
                        + command.getName() + "'s help</gold> ==================="
        );
    }


    @Override
    public Component getFooter(Command<?> command) {
        return Messages.getMsg("<dark_gray><bold><strikethrough>===============================================");
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
