package dev.velix.imperat;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import dev.velix.imperat.context.Source;
import net.kyori.adventure.text.ComponentLike;

public class VelocitySource implements Source {

    private final CommandSource origin;

    VelocitySource(CommandSource origin) {
        this.origin = origin;
    }

    @Override
    public String name() {
        return origin instanceof Player pl ? pl.getUsername() : "CONSOLE";
    }

    @Override
    public CommandSource origin() {
        return origin;
    }

    @Override
    public void reply(String message) {
        origin.sendRichMessage(message);
    }

    public void reply(ComponentLike component) {
        origin.sendMessage(component);
    }

    @Override
    public void warn(String message) {
        origin.sendRichMessage("<yellow>" + message + "</yellow>");
    }

    @Override
    public void error(String message) {
        origin.sendRichMessage("<red>" + message + "</red>");
    }

    @Override
    public boolean isConsole() {
        return origin instanceof ConsoleCommandSource;
    }

    public ConsoleCommandSource asConsole() {
        return (ConsoleCommandSource) origin;
    }

    public Player asPlayer() {
        return (Player) origin;
    }
}
