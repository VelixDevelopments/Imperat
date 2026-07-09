package studio.mevera.imperat.bukkit.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import studio.mevera.imperat.BaseBrigadierManager;
import studio.mevera.imperat.BukkitCommandSource;
import studio.mevera.imperat.BukkitImperat;
import studio.mevera.imperat.bukkit.test.commands.PermissionBrigadierCmd;
import studio.mevera.imperat.command.Command;
import studio.mevera.imperat.command.CommandPathway;
import studio.mevera.imperat.command.arguments.Argument;

import java.util.Objects;

/**
 * Verifies that the Brigadier node tree emitted by {@link BaseBrigadierManager}
 * carries {@code requires} predicates that hide subcommand literals whose only
 * executable pathway is locked behind a permission. Minecraft filters the tree
 * sent to each client via {@code CommandNode#canUse}, so these assertions mirror
 * exactly what gates client-side tab completion in-game.
 */
@DisplayName("Bukkit Brigadier Permission Visibility Tests")
class BukkitBrigadierPermissionVisibilityTest {

    private ServerMock server;
    private TestImperatPlugin plugin;
    private BukkitImperat imperat;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestImperatPlugin.class);
        imperat = plugin.getImperat();
        imperat.registerCommand(new PermissionBrigadierCmd());
        player = server.addPlayer("TestPlayer");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Should hide subcommand literal when its executable pathway permission is missing")
    void testLockedSubcommandHiddenWithoutPermission() {
        LiteralCommandNode<PlayerMock> root = buildTree();

        CommandNode<PlayerMock> open = root.getChild("open");
        CommandNode<PlayerMock> restricted = root.getChild("restricted");
        assertNotNull(open);
        assertNotNull(restricted);

        assertTrue(open.canUse(player));
        assertFalse(restricted.canUse(player));
    }

    @Test
    @DisplayName("Should show subcommand literal when the executable pathway permission is granted")
    void testLockedSubcommandVisibleWithPermission() {
        player.addAttachment(plugin, "permbrig.restricted", true);

        LiteralCommandNode<PlayerMock> root = buildTree();

        CommandNode<PlayerMock> restricted = root.getChild("restricted");
        assertNotNull(restricted);
        assertTrue(restricted.canUse(player));
    }

    @Test
    @DisplayName("Should show programmatically-built subcommands (execution without method element)")
    void testProgrammaticSubcommandVisible() {
        Command<BukkitCommandSource> sub = Command.create(imperat, "progsub")
                                                   .pathway(CommandPathway.<BukkitCommandSource>builder()
                                                                    .arguments(Argument.requiredText("name"))
                                                                    .execute((source, ctx) -> {}))
                                                   .build();
        Command<BukkitCommandSource> parent = Command.create(imperat, "progroot")
                                                      .subCommand(sub)
                                                      .build();
        imperat.registerSimpleCommand(parent);

        TestBrigadierManager manager = new TestBrigadierManager(imperat);
        LiteralCommandNode<PlayerMock> root = manager.parseCommandIntoNode(parent);

        CommandNode<PlayerMock> subNode = root.getChild("progsub");
        assertNotNull(subNode);
        assertTrue(subNode.canUse(player));
    }

    private LiteralCommandNode<PlayerMock> buildTree() {
        TestBrigadierManager manager = new TestBrigadierManager(imperat);
        Command<BukkitCommandSource> command = Objects.requireNonNull(imperat.getCommand("permbrig"));
        return manager.parseCommandIntoNode(command);
    }

    private static final class TestBrigadierManager extends BaseBrigadierManager<BukkitCommandSource> {

        private final BukkitImperat imperat;

        private TestBrigadierManager(BukkitImperat imperat) {
            super(imperat);
            this.imperat = imperat;
        }

        @Override
        public BukkitCommandSource wrapCommandSource(Object commandSource) {
            return imperat.wrapSender(commandSource);
        }

        @Override
        public ArgumentType<?> getArgumentType(Argument<BukkitCommandSource> parameter) {
            return parameter.isGreedy() ? StringArgumentType.greedyString() : StringArgumentType.word();
        }
    }
}
