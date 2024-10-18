package dev.velix.imperat.type;

import com.google.common.base.Charsets;
import com.mojang.authlib.GameProfile;
import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.type.BaseParameterType;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.UnknownOfflinePlayerException;
import dev.velix.imperat.exception.UnknownPlayerException;
import dev.velix.imperat.util.TypeWrap;
import net.minecraft.server.v1_13_R2.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spigotmc.SpigotConfig;

import java.util.UUID;

public class ParameterOfflinePlayer extends BaseParameterType<BukkitSource, OfflinePlayer> {

    protected final boolean ifCachedOnly;

    public ParameterOfflinePlayer(boolean ifCachedOnly) {
        super(TypeWrap.of(OfflinePlayer.class));
        this.ifCachedOnly = ifCachedOnly;
    }

    public ParameterOfflinePlayer() {
        this(false);
    }

    @Override
    @SuppressWarnings("deprecation")
    public @Nullable OfflinePlayer resolve(
        ExecutionContext<BukkitSource> context,
        @NotNull CommandInputStream<BukkitSource> commandInputStream
    ) throws ImperatException {
        String name = commandInputStream.currentRaw().orElse(null);
        if (name == null) return null;

        if (name.length() > 16) {
            throw new UnknownPlayerException(name);
        }

        GameProfile profile = null;
        if (MinecraftServer.getServer().getOnlineMode() || SpigotConfig.bungee) {
            profile = MinecraftServer.getServer().getUserCache().getProfile(name);
        }

        if (profile == null) {

            if (!ifCachedOnly) {
                profile = new GameProfile(UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8)), name);
            } else {
                throw new UnknownOfflinePlayerException("Unknown offline player");
            }
        }

        return ((CraftServer) Bukkit.getServer()).getOfflinePlayer(profile);
    }

    @Override
    public boolean matchesInput(String input, CommandParameter<BukkitSource> parameter) {
        return input.length() <= 16;
    }


}
