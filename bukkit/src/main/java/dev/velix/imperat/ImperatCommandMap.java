package dev.velix.imperat;

import com.destroystokyo.paper.event.server.ServerExceptionEvent;
import com.destroystokyo.paper.exception.ServerTabCompleteException;
import dev.velix.imperat.type.Version;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class ImperatCommandMap extends SimpleCommandMap {

    private final BukkitImperat imperat;
    private final Server server;

    ImperatCommandMap(BukkitImperat imperat, @NotNull Server server) {
        super(server);
        this.imperat = imperat;
        this.server = server;
    }

    @Override
    public boolean register(@NotNull String label, @NotNull String fallbackPrefix, @NotNull Command command) {
        //command.timings = TimingsManager.getCommandTiming(fallbackPrefix, command);
        label = label.toLowerCase(Locale.ENGLISH).trim();
        fallbackPrefix = fallbackPrefix.toLowerCase(Locale.ENGLISH).trim();
        boolean registered = this.register(label, command, false, fallbackPrefix);

        Iterator<String> iterator = command.getAliases().iterator();
        while(iterator.hasNext()) {
            if (!this.register(iterator.next(), command, true, fallbackPrefix)) {
                iterator.remove();
            }
        }

        if (!registered) {
            command.setLabel(fallbackPrefix + ":" + label);
        }

        command.register(this);
        return registered;
    }

    private synchronized boolean register(@NotNull String label, @NotNull Command command, boolean isAlias, @NotNull String fallbackPrefix) {
        this.knownCommands.put(fallbackPrefix + ":" + label, command);
        if(!(command instanceof InternalBukkitCommand) ) {
            imperat.registerCommand(BukkitUtil.convertBukkitCmdToImperatCmd(command));
        }
        if ((command instanceof BukkitCommand || isAlias) && this.knownCommands.containsKey(label)) {
            return false;
        } else {
            Command conflict = this.knownCommands.get(label);
            if (conflict != null && conflict.getLabel().equals(label)) {
                return false;
            } else {
                if (!isAlias) {
                    command.setLabel(label);
                }

                this.knownCommands.put(label, command);
                return true;
            }
        }
    }

    @Override
    public boolean dispatch(@NotNull CommandSender sender, @NotNull String commandLine) throws CommandException {
        imperat.dispatch(imperat.wrapSender(sender), commandLine);
        return true;
    }

    @Override
    public @Nullable List<String> tabComplete(@NotNull CommandSender sender, @NotNull String cmdLine, @Nullable Location location) {
        int spaceIndex = cmdLine.indexOf(32);
        if (spaceIndex == -1) {
            ArrayList<String> completions = new ArrayList<>();
            Map<String, Command> knownCommands = this.knownCommands;
            String prefix = sender instanceof Player ? "/" : "";

            for(Map.Entry<String, Command> commandEntry : knownCommands.entrySet()) {
                Command command = commandEntry.getValue();
                if (command.testPermissionSilent(sender)) {
                    String name = commandEntry.getKey();
                    if (StringUtil.startsWithIgnoreCase(name, cmdLine)) {
                        completions.add(prefix + name);
                    }
                }
            }

            completions.sort(String.CASE_INSENSITIVE_ORDER);
            return completions;
        } else {
            String commandName = cmdLine.substring(0, spaceIndex);
            Command target = this.getCommand(commandName);
            String[] args = cmdLine.substring(spaceIndex + 1).split(" ", -1);

            if(target instanceof InternalBukkitCommand imperatCmd) {
                //A command registered by imperat
                //running default code
                if (!target.testPermissionSilent(sender)) {
                    return null;
                } else {

                    try {
                        return imperat.autoComplete(imperatCmd.imperatCommand, imperat.wrapSender(sender), commandName,args).join();
                    } catch (CommandException ex) {
                        throw ex;
                    } catch (Throwable ex) {
                        String msg = "Unhandled exception executing tab-completer for '" + cmdLine + "' in " + target;
                        if(Version.IS_PAPER) {
                            this.server.getPluginManager()
                                    .callEvent(new ServerExceptionEvent(new ServerTabCompleteException(msg, ex, target, sender, args)));
                        }
                        throw new CommandException(msg, ex);
                    }
                }
            }else {
                if(target == null)
                    return null;
                //A command registered externally
                //Finding imperat cmd from its name
                var imperatCmd = imperat.getCommand(target.getName());
                if(imperatCmd == null) {
                    return null;
                }
                return imperat.autoComplete(imperatCmd, imperat.wrapSender(sender), commandName, args).join();
            }
        }
    }
}
