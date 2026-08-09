package studio.mevera.imperat;

import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import studio.mevera.imperat.command.Command;
import studio.mevera.imperat.command.CommandPathway;
import studio.mevera.imperat.util.StringUtils;

import java.util.Collections;
import java.util.List;

@ApiStatus.Internal
public final class InternalBukkitCommand<S extends BukkitCommandSource> extends org.bukkit.command.Command implements PluginIdentifiableCommand {

    public final @NotNull Command<S> imperatCommand;
    @NotNull
    private final BukkitImperat<S> dispatcher;

    public InternalBukkitCommand(
            final @NotNull BukkitImperat<S> dispatcher,
            final @NotNull Command<S> imperatCommand
    ) {
        super(
                imperatCommand.getName(),
                imperatCommand.getDescription().getValueOrElse(""),
                CommandPathway.format((String) null, imperatCommand.getDefaultPathway()),
                imperatCommand.aliases()
        );
        this.dispatcher = dispatcher;
        this.imperatCommand = imperatCommand;
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return dispatcher.getPlatform();
    }

    @Nullable
    @Override
    public String getPermission() {
        return imperatCommand.getPrimaryPermission();
    }

    @Override
    public boolean execute(
            @NotNull CommandSender sender,
            @NotNull String label,
            String @NonNull [] raw
    ) {
        final S source = dispatcher.wrapSender(sender);
        dispatcher.execute(source, this.imperatCommand, StringUtils.stripNamespace(label), raw);
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(
            final @NotNull CommandSender sender,
            final @NotNull String alias,
            final String @NonNull [] args
    ) throws IllegalArgumentException {
        if (Version.SUPPORTS_PAPER_ASYNC_TAB_COMPLETION) {
            //supports async tab completion
            //we will tab complete from the async tab completion event
            return Collections.emptyList();
        }
        final String input = alias + " " + String.join(" ", args);
        return dispatcher.autoComplete(
                dispatcher.wrapSender(sender),
                input
        ).join();
    }

}