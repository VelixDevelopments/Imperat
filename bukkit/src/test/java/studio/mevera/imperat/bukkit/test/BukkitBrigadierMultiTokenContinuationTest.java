package studio.mevera.imperat.bukkit.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import studio.mevera.imperat.bukkit.test.commands.MultiTokenBrigadierCmd;
import studio.mevera.imperat.command.Command;
import studio.mevera.imperat.command.arguments.Argument;

import java.util.List;
import java.util.Objects;

/**
 * Regression coverage for the multi-token Brigadier chain: a fixed-arity
 * positional ({@code getNumberOfParametersToConsume > 1}) must render as N
 * Brigadier segments with child scopes and flags attached to the DEEPEST
 * segment — and building the tree must never throw.
 *
 * <p><b>Pre-fix:</b> the token fillers were linked top-down, so Brigadier
 * snapshotted each filler as a childless node at link time. The built chain
 * collapsed to two levels, walking it to the deepest segment threw
 * {@link java.util.NoSuchElementException}, and the continuations attached
 * to the deepest builder afterwards (child scopes) silently vanished from
 * the graph.</p>
 */
@DisplayName("Bukkit Brigadier Multi-Token Continuation Tests")
class BukkitBrigadierMultiTokenContinuationTest {

    private BukkitImperat<BukkitCommandSource> imperat;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        ServerMock server = MockBukkit.mock();
        TestImperatPlugin plugin = MockBukkit.load(TestImperatPlugin.class);
        imperat = plugin.getImperat();
        imperat.registerCommand(new MultiTokenBrigadierCmd());
        player = server.addPlayer("TestPlayer");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Should build the full 4-token chain without throwing")
    void testBuildsFourTokenChain() {
        LiteralCommandNode<PlayerMock> root = buildTree();

        CommandNode<PlayerMock> coord = root.getChild("coord");
        assertNotNull(coord, "head node for multi-token argument missing");
        CommandNode<PlayerMock> part2 = coord.getChild("coord_part2");
        CommandNode<PlayerMock> part3 = part2.getChild("coord_part3");
        CommandNode<PlayerMock> part4 = part3.getChild("coord_part4");
        assertNotNull(part2, "second token segment missing");
        assertNotNull(part3, "third token segment missing");
        assertNotNull(part4, "fourth token segment missing");
    }

    @Test
    @DisplayName("Should attach child scopes and optionals only to the deepest segment")
    void testContinuationsOnDeepestSegment() {
        LiteralCommandNode<PlayerMock> root = buildTree();
        CommandNode<PlayerMock> coord = root.getChild("coord");

        // Pre-fix the child scope + its optional were dropped entirely
        // (NoSuchElementException during build, or silent loss).
        CommandNode<PlayerMock> label = deepestSegment(root).getChild("label");
        assertNotNull(label, "child scope lost after multi-token argument");
        assertNotNull(label.getChild("world"), "optional argument lost after multi-token argument");
        // The child must only surface after ALL tokens, not at intermediate segments.
        assertNull(coord.getChild("label"), "child scope surfaced before the full token chain");
    }

    @Test
    @DisplayName("Should attach flags to the deepest segment and optional depth")
    void testFlagsOnDeepestAndOptionalDepth() {
        LiteralCommandNode<PlayerMock> root = buildTree();

        assertNotNull(deepestSegment(root).getChild("flag"), "flag node missing at deepest segment");
        CommandNode<PlayerMock> world = deepestSegment(root).getChild("label").getChild("world");
        assertNotNull(world, "optional argument missing after multi-token argument");
        assertNotNull(world.getChild("flag"), "flag node missing at optional depth");
    }

    @Test
    @DisplayName("Should complete flags after all four tokens")
    void testCompletionsAfterFullChain() {
        var suggestions = complete("mt 1 2 3 4 ");
        assertTrue(suggestions.contains("-mode"), "flag should be suggested after all tokens, got " + suggestions);
        assertTrue(suggestions.contains("-m"), "flag alias should be suggested after all tokens, got " + suggestions);
    }

    private CommandNode<PlayerMock> deepestSegment(LiteralCommandNode<PlayerMock> root) {
        CommandNode<PlayerMock> coord = root.getChild("coord");
        CommandNode<PlayerMock> part2 = coord.getChild("coord_part2");
        CommandNode<PlayerMock> part3 = part2.getChild("coord_part3");
        return part3.getChild("coord_part4");
    }

    private LiteralCommandNode<PlayerMock> buildTree() {
        TestBrigadierManager manager = new TestBrigadierManager(imperat);
        Command<BukkitCommandSource> command = Objects.requireNonNull(imperat.getCommand("mt"));
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
