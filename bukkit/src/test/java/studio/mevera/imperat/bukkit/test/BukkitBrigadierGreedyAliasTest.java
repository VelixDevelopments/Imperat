package studio.mevera.imperat.bukkit.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestion;
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
import studio.mevera.imperat.bukkit.test.commands.GreedyAliasBrigadierCmd;
import studio.mevera.imperat.command.Command;
import studio.mevera.imperat.command.arguments.Argument;

import java.util.List;
import java.util.Objects;

/**
 * Regression tests for greedy-argument detection in
 * {@link BaseBrigadierManager}'s suggestion-context construction. The active
 * command / argument-start offsets must be computed from token positions, not
 * {@code String#indexOf} of the command's primary name — otherwise inline-flag
 * completions break whenever a subcommand is reached through an alias (the
 * primary-name length is added at the alias's index) or the name happens to
 * appear as a substring of an earlier token.
 */
@DisplayName("Bukkit Brigadier Greedy Alias Detection Tests")
class BukkitBrigadierGreedyAliasTest {

    private ServerMock server;
    private TestImperatPlugin plugin;
    private BukkitImperat imperat;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestImperatPlugin.class);
        imperat = plugin.getImperat();
        imperat.registerCommand(new GreedyAliasBrigadierCmd());
        player = server.addPlayer("TestPlayer");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Should give identical inline flag-value completions via subcommand primary name and alias")
    void testInlineFlagCompletionMatchesBetweenNameAndAlias() {
        var viaName = complete("chat send -sc=");
        var viaAlias = complete("chat s -sc=");

        assertTrue(viaName.containsAll(List.of("-sc=kindergarten", "-sc=castle")),
                "expected inline flag values in " + viaName);
        assertEquals(viaName, viaAlias,
                "alias completion must match primary-name completion");
    }

    @Test
    @DisplayName("Should give identical completions after greedy text via subcommand primary name and alias")
    void testGreedyPrefixCompletionMatchesBetweenNameAndAlias() {
        var viaName = complete("chat send hello world -sc=");
        var viaAlias = complete("chat s hello world -sc=");

        assertEquals(viaName, viaAlias,
                "alias completion must match primary-name completion after greedy text");
    }

    @Test
    @DisplayName("Should not duplicate inline flag-value completions across sibling nodes")
    void testInlineFlagCompletionHasNoDuplicates() {
        var suggestions = complete("chat send -sc=");

        assertEquals(List.of("-sc=castle", "-sc=kindergarten"), suggestions,
                "expected deduplicated inline flag values, got " + suggestions);
    }

    @Test
    @DisplayName("Should complete inline flag values after greedy text")
    void testInlineFlagCompletionAfterGreedyText() {
        var suggestions = complete("chat send hello world -sc=");

        assertEquals(List.of("-sc=castle", "-sc=kindergarten"), suggestions,
                "expected inline flag values after greedy text, got " + suggestions);
    }

    private List<String> complete(String input) {
        TestBrigadierManager manager = new TestBrigadierManager(imperat);
        Command<BukkitCommandSource> command = Objects.requireNonNull(imperat.getCommand("chat"));
        var dispatcher = new CommandDispatcher<PlayerMock>();
        dispatcher.getRoot().addChild(manager.parseCommandIntoNode(command));
        return dispatcher.getCompletionSuggestions(dispatcher.parse(input, player))
                       .join()
                       .getList()
                       .stream()
                       .map(Suggestion::getText)
                       .toList();
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
