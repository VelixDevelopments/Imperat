package studio.mevera.imperat.bukkit.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.suggestion.Suggestion;
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
import studio.mevera.imperat.bukkit.test.commands.MessageBrigadierCmd;
import studio.mevera.imperat.command.Command;
import studio.mevera.imperat.command.arguments.Argument;

import java.util.List;
import java.util.Objects;

/**
 * Positional-before-greedy suggestion positions must suggest the positional's
 * own completions (player names) until the greedy argument is actually
 * reached — never one argument late.
 */
@DisplayName("Bukkit Brigadier Positional-before-Greedy Suggestion Tests")
class BukkitBrigadierPositionalBeforeGreedyTest {

    private BukkitImperat<BukkitCommandSource> imperat;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        ServerMock server = MockBukkit.mock();
        TestImperatPlugin plugin = MockBukkit.load(TestImperatPlugin.class);
        imperat = plugin.getImperat();
        imperat.registerCommand(new MessageBrigadierCmd());
        player = server.addPlayer("TestPlayer");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Suggests target values at the empty target position")
    void suggestsTargetsAtEmptyPosition() {
        var suggestions = complete("msgtest message ");
        assertTrue(suggestions.containsAll(List.of("TestPlayer", "Notch", "Herobrine")),
                "expected target values, got " + suggestions);
    }

    @Test
    @DisplayName("Filters target values by the typed prefix")
    void filtersTargetsByPrefix() {
        var suggestions = complete("msgtest message N");
        assertTrue(suggestions.contains("Notch") && !suggestions.contains("Herobrine"),
                "expected only 'Notch', got " + suggestions);
    }

    @Test
    @DisplayName("Suggests greedy values only after the target is complete")
    void suggestsGreedyAfterTarget() {
        var suggestions = complete("msgtest message Notch ");
        assertTrue(suggestions.containsAll(List.of("hello", "world", "-direct")),
                "expected greedy values + switch, got " + suggestions);
        assertTrue(!suggestions.contains("TestPlayer"),
                "target suggestions should be gone, got " + suggestions);
    }

    @Test
    @DisplayName("Filters greedy values by the typed prefix")
    void filtersGreedyByPrefix() {
        var suggestions = complete("msgtest message Notch he");
        assertTrue(suggestions.contains("hello") && !suggestions.contains("world"),
                "expected only 'hello', got " + suggestions);
    }

    private List<String> complete(String input) {
        TestBrigadierManager manager = new TestBrigadierManager(imperat);
        Command<BukkitCommandSource> command = Objects.requireNonNull(imperat.getCommand("msgtest"));
        var dispatcher = new CommandDispatcher<PlayerMock>();
        dispatcher.getRoot().addChild(manager.parseCommandIntoNode(command));
        var raw = dispatcher.getCompletionSuggestions(dispatcher.parse(input, player))
                       .join()
                       .getList();
        return raw.stream()
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
            return getStringArgType(parameter);
        }
    }
}
