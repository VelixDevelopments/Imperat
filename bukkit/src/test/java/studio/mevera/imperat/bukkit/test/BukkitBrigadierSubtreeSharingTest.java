package studio.mevera.imperat.bukkit.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.jspecify.annotations.NonNull;
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
import studio.mevera.imperat.bukkit.test.commands.OptionalDuplicationCmd;
import studio.mevera.imperat.command.Command;
import studio.mevera.imperat.command.arguments.Argument;

import java.util.List;
import java.util.Objects;

/**
 * The Brigadier tree must be a DAG, not a tree of copies: a child scope
 * reachable both directly from its parent anchor and after each optional
 * argument must be the SAME node instance at every attach point. Rebuilding
 * the subtree per optional position multiplies node count by (optionals + 1)
 * at every level, bloating the command-tree packet sent to every client.
 */
@DisplayName("Bukkit Brigadier Subtree Sharing Tests")
class BukkitBrigadierSubtreeSharingTest {

    private BukkitImperat<BukkitCommandSource> imperat;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        ServerMock server = MockBukkit.mock();
        TestImperatPlugin plugin = MockBukkit.load(TestImperatPlugin.class);
        imperat = plugin.getImperat();
        imperat.registerCommand(new OptionalDuplicationCmd());
        player = server.addPlayer("TestPlayer");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Should reuse the same subcommand node instance at every optional depth")
    void testSubcommandNodeSharedAcrossOptionalDepths() {
        LiteralCommandNode<PlayerMock> root = buildTree();

        CommandNode<PlayerMock> subDirect = root.getChild("sub");
        CommandNode<PlayerMock> first = root.getChild("first");
        assertNotNull(subDirect);
        assertNotNull(first);
        CommandNode<PlayerMock> subUnderFirst = first.getChild("sub");
        CommandNode<PlayerMock> second = first.getChild("second");
        assertNotNull(subUnderFirst);
        assertNotNull(second);
        CommandNode<PlayerMock> subUnderSecond = second.getChild("sub");
        assertNotNull(subUnderSecond);

        assertSame(subDirect, subUnderFirst, "sub node duplicated under first optional");
        assertSame(subDirect, subUnderSecond, "sub node duplicated under second optional");
    }

    @Test
    @DisplayName("Should reuse the same alias clone instance at every optional depth")
    void testAliasNodeSharedAcrossOptionalDepths() {
        LiteralCommandNode<PlayerMock> root = buildTree();

        CommandNode<PlayerMock> aliasDirect = root.getChild("sb");
        CommandNode<PlayerMock> aliasUnderFirst = root.getChild("first").getChild("sb");
        assertNotNull(aliasDirect);
        assertNotNull(aliasUnderFirst);

        assertSame(aliasDirect, aliasUnderFirst, "alias clone duplicated under optional");
    }

    @Test
    @DisplayName("Should keep completions intact with shared subtrees")
    void testCompletionsUnchangedWithSharing() {
        // Brigadier pads root-listed literal texts with the trailing space
        // after range merging, so compare trimmed forms.
        var atRoot = complete("dup ").stream().map(String::trim).toList();
        assertTrue(atRoot.containsAll(List.of("sub", "sb")), "expected sub + sb in " + atRoot);

        var afterOptional = complete("dup hello ").stream().map(String::trim).toList();
        assertTrue(afterOptional.containsAll(List.of("sub", "sb")),
                "expected sub + sb after optional in " + afterOptional);
    }

    private LiteralCommandNode<PlayerMock> buildTree() {
        TestBrigadierManager manager = new TestBrigadierManager(imperat);
        Command<BukkitCommandSource> command = Objects.requireNonNull(imperat.getCommand("dup"));
        return manager.parseCommandIntoNode(command);
    }

    private List<String> complete(String input) {
        var dispatcher = new CommandDispatcher<PlayerMock>();
        dispatcher.getRoot().addChild(buildTree());
        return dispatcher.getCompletionSuggestions(dispatcher.parse(input, player))
                       .join()
                       .getList()
                       .stream()
                       .map(Suggestion::getText)
                       .toList();
    }

    private static final class TestBrigadierManager extends BaseBrigadierManager<BukkitCommandSource> {

        private final BukkitImperat<BukkitCommandSource> imperat;

        private TestBrigadierManager(BukkitImperat<BukkitCommandSource> imperat) {
            super(imperat);
            this.imperat = imperat;
        }

        @Override
        public BukkitCommandSource wrapCommandSource(Object commandSource) {
            return imperat.wrapSender(commandSource);
        }

        @Override
        public @NonNull ArgumentType<?> getArgumentType(Argument<BukkitCommandSource> parameter) {
            return parameter.isGreedy() ? StringArgumentType.greedyString() : StringArgumentType.word();
        }
    }
}
