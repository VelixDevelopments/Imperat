package studio.mevera.imperat.bukkit.test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.bukkit.GameMode;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import studio.mevera.imperat.BukkitCommandSource;
import studio.mevera.imperat.command.arguments.Argument;
import studio.mevera.imperat.command.arguments.type.ArgumentType;
import studio.mevera.imperat.context.ArgumentTypeRegistry;
import studio.mevera.imperat.command.arguments.type.Cursor;
import studio.mevera.imperat.context.CommandContext;
import studio.mevera.imperat.util.priority.Priority;

import java.util.function.Supplier;

@DisplayName("Argument type registration ordering (native vs user override)")
class ArgTypeOrderingTest {

    @Test
    @DisplayName("Native registered first (ctor), user post-build at HIGH -> user wins")
    void nativeFirstThenUserLoses() {
        ArgumentTypeRegistry<BukkitCommandSource> reg = ArgumentTypeRegistry.createDefault();
        Supplier<ArgumentType<BukkitCommandSource, GameMode>> nativeS = NativeArg::new;
        Supplier<ArgumentType<BukkitCommandSource, GameMode>> userS = UserArg::new;
        reg.registerResolver(GameMode.class, nativeS, Priority.LOW.plus(1));
        reg.registerResolver(GameMode.class, userS, Priority.HIGH);

        assertInstanceOf(
                UserArg.class,
                reg.<GameMode>getResolver(GameMode.class).orElseThrow(),
                "user post-build override must win even when native default was registered first"
        );
    }

    @Test
    @DisplayName("User registered via builder chain BEFORE build -> user wins")
    void userFirstThenNativeWins() {
        ArgumentTypeRegistry<BukkitCommandSource> reg = ArgumentTypeRegistry.createDefault();
        Supplier<ArgumentType<BukkitCommandSource, GameMode>> userS = UserArg::new;
        Supplier<ArgumentType<BukkitCommandSource, GameMode>> nativeS = NativeArg::new;
        reg.registerResolver(GameMode.class, userS, Priority.HIGH);
        reg.registerResolver(GameMode.class, nativeS, Priority.LOW.plus(1));

        assertInstanceOf(
                UserArg.class,
                reg.<GameMode>getResolver(GameMode.class).orElseThrow(),
                "user (inserted first) should win"
        );
    }

    @Test
    @DisplayName("Without a user override the native default still resolves (beats the LOW enum handler)")
    void nativeDefaultWinsWithoutOverride() {
        ArgumentTypeRegistry<BukkitCommandSource> reg = ArgumentTypeRegistry.createDefault();
        reg.registerResolver(GameMode.class, NativeArg::new, Priority.LOW.plus(1));

        assertInstanceOf(
                NativeArg.class,
                reg.<GameMode>getResolver(GameMode.class).orElseThrow(),
                "native default must resolve when the user registers nothing"
        );
    }

    static class NativeArg extends ArgumentType<BukkitCommandSource, GameMode> {
        NativeArg() {
            super(GameMode.class);
        }

        @Override
        public GameMode parse(
                @NonNull CommandContext<BukkitCommandSource> context,
                @NonNull Argument<BukkitCommandSource> argument,
                @NonNull Cursor<BukkitCommandSource> cursor
        ) {
            throw new UnsupportedOperationException();
        }
    }

    static class UserArg extends ArgumentType<BukkitCommandSource, GameMode> {
        UserArg() {
            super(GameMode.class);
        }

        @Override
        public GameMode parse(
                @NonNull CommandContext<BukkitCommandSource> context,
                @NonNull Argument<BukkitCommandSource> argument,
                @NonNull Cursor<BukkitCommandSource> cursor
        ) {
            throw new UnsupportedOperationException();
        }
    }
}
