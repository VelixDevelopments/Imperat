package studio.mevera.imperat.bukkit.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
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
import studio.mevera.imperat.bukkit.test.commands.GreedyWithSwitchBrigadierCmd;
import studio.mevera.imperat.command.Command;
import studio.mevera.imperat.command.arguments.Argument;

import java.util.List;
import java.util.Objects;

/**
 * A greedy positional followed by a {@code @Switch} must still surface both
 * the greedy argument's own suggestions AND the switch, at every relevant
 * position — the flag is folded into the greedy node's suggester so no
 * cyclic {@code <flag>} node is emitted beside the greedy string.
 */
@DisplayName("Bukkit Brigadier Greedy + Switch Suggestion Tests")
class BukkitBrigadierGreedyWithSwitchTest {

    private BukkitImperat<BukkitCommandSource> imperat;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        ServerMock server = MockBukkit.mock();
        TestImperatPlugin plugin = MockBukkit.load(TestImperatPlugin.class);
        imperat = plugin.getImperat();
        imperat.registerCommand(new GreedyWithSwitchBrigadierCmd());
        player = server.addPlayer("TestPlayer");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Suggests greedy targets AND the switch at the greedy position")
    void suggestsTargetsAndSwitch() {
        var suggestions = complete("greedysw migrate ");
        assertTrue(suggestions.containsAll(List.of("alpha", "beta", "gamma", "-shallow")),
                "expected targets and the switch, got " + suggestions);
    }

    @Test
    @DisplayName("Filters greedy targets by the typed prefix")
    void filtersByPrefix() {
        var suggestions = complete("greedysw migrate al");
        assertTrue(suggestions.contains("alpha") && !suggestions.contains("beta"),
                "expected only 'alpha', got " + suggestions);
    }

    @Test
    @DisplayName("Continues suggesting greedy targets after the switch is consumed")
    void suggestsTargetsAfterSwitch() {
        var suggestions = complete("greedysw migrate -shallow ");
        assertTrue(suggestions.containsAll(List.of("alpha", "beta", "gamma")),
                "expected targets after the switch, got " + suggestions);
    }

    private List<String> complete(String input) {
        TestBrigadierManager manager = new TestBrigadierManager(imperat);
        Command<BukkitCommandSource> command = Objects.requireNonNull(imperat.getCommand("greedysw"));
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
