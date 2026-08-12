package studio.mevera.imperat.bukkit.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.Suggestion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import studio.mevera.imperat.BukkitCommandSource;
import studio.mevera.imperat.BukkitImperat;
import studio.mevera.imperat.backend.modern.ModernPaperBrigadierManager;
import studio.mevera.imperat.bukkit.test.commands.RealMessageBrigadierCmd;
import studio.mevera.imperat.command.Command;

import java.util.List;
import java.util.Objects;

/**
 * Runs the real ItsMyConfig command shape through the actual
 * {@link ModernPaperBrigadierManager} — multi-subcommand class, permissions,
 * root-level aliases, strict selector arg type — and checks every suggestion
 * position of {@code /itsmyconfig message <target> [-direct] <message>}.
 */
@DisplayName("Bukkit Brigadier RealMessage Shape Tests")
class BukkitBrigadierRealMessageShapeTest {

    private BukkitImperat<BukkitCommandSource> imperat;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        ServerMock server = MockBukkit.mock();
        TestImperatPlugin plugin = MockBukkit.load(TestImperatPlugin.class);
        imperat = plugin.getImperat();
        imperat.registerCommand(new RealMessageBrigadierCmd());
        player = server.addPlayer("TestPlayer");
        for (String perm : List.of(
                "itsmyconfig.admin",
                "itsmyconfig.reload",
                "itsmyconfig.message",
                "itsmyconfig.parse",
                "itsmyconfig.debug",
                "itsmyconfig.config"
        )) {
            player.addAttachment(plugin, perm, true);
        }
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Suggests subcommands at the root")
    void suggestsSubcommandsAtRoot() {
        var suggestions = complete("itsmyconfig ", "itsmyconfig");
        assertTrue(suggestions.containsAll(List.of("help", "reload", "message", "parse", "debug", "config")),
                "expected subcommands, got " + suggestions);
    }

    @Test
    @DisplayName("Suggests target values at the empty target position")
    void suggestsTargetsAtEmptyPosition() {
        var suggestions = complete("itsmyconfig message ", "itsmyconfig");
        assertTrue(suggestions.containsAll(List.of("TestPlayer", "Notch", "Herobrine")),
                "expected target values, got " + suggestions);
    }

    @Test
    @DisplayName("Filters target values by the typed prefix")
    void filtersTargetsByPrefix() {
        var suggestions = complete("itsmyconfig message N", "itsmyconfig");
        assertTrue(suggestions.contains("Notch") && !suggestions.contains("Herobrine"),
                "expected only 'Notch', got " + suggestions);
    }

    @Test
    @DisplayName("Suggests greedy values after the target is complete")
    void suggestsGreedyAfterTarget() {
        var suggestions = complete("itsmyconfig message TestPlayer ", "itsmyconfig");
        assertTrue(suggestions.containsAll(List.of("hello", "world", "-direct")),
                "expected greedy values + switch, got " + suggestions);
        assertTrue(!suggestions.contains("Notch"),
                "target suggestions should be gone, got " + suggestions);
    }

    @Test
    @DisplayName("Suggests nothing target-like after greedy is started")
    void noPlayersAfterGreedyStarted() {
        var suggestions = complete("itsmyconfig message TestPlayer hel ", "itsmyconfig");
        assertTrue(!suggestions.contains("Notch") && !suggestions.contains("Herobrine"),
                "player suggestions leaked into the greedy position, got " + suggestions);
    }

    @Test
    @DisplayName("Root-level alias /message target position works")
    void aliasTargetPosition() {
        var suggestions = complete("message ", "message");
        assertTrue(suggestions.containsAll(List.of("TestPlayer", "Notch", "Herobrine")),
                "expected target values on alias, got " + suggestions);
    }

    @Test
    @DisplayName("message subtree emits no cyclic flag node (greedy fold)")
    void messageTreeHasNoFlagNode() {
        ModernPaperBrigadierManager<BukkitCommandSource> manager =
                new ModernPaperBrigadierManager<>(imperat);
        Command<BukkitCommandSource> command = Objects.requireNonNull(imperat.getCommand("itsmyconfig"));
        var dispatcher = new CommandDispatcher<PlayerMock>();
        dispatcher.getRoot().addChild(manager.parseCommandIntoNode(command));
        var messageNode = dispatcher.getRoot().getChild("itsmyconfig").getChild("message");
        assertTrue(messageNode != null, "message node missing");
        assertTrue(!containsNodeNamed(messageNode, "flag"),
                "message subtree must not contain the unserializable flag node; "
                        + "flags must be folded into the greedy. tree=" + treeNames(messageNode));
    }

    private boolean containsNodeNamed(com.mojang.brigadier.tree.CommandNode<?> node, String name) {
        if (node.getName().equals(name)) {
            return true;
        }
        for (var child : node.getChildren()) {
            if (containsNodeNamed(child, name)) {
                return true;
            }
        }
        return false;
    }

    private String treeNames(com.mojang.brigadier.tree.CommandNode<?> node) {
        StringBuilder sb = new StringBuilder();
        for (var child : node.getChildren()) {
            sb.append(child.getName()).append(" ");
        }
        return sb.toString();
    }

    private List<String> complete(String input, String rootName) {
        ModernPaperBrigadierManager<BukkitCommandSource> manager =
                new ModernPaperBrigadierManager<>(imperat);
        Command<BukkitCommandSource> command = Objects.requireNonNull(imperat.getCommand(rootName));
        var dispatcher = new CommandDispatcher<PlayerMock>();
        dispatcher.getRoot().addChild(manager.parseCommandIntoNode(command));
        var raw = dispatcher.getCompletionSuggestions(dispatcher.parse(input, player))
                       .join()
                       .getList();
        return raw.stream()
                       .map(Suggestion::getText)
                       .toList();
    }
}