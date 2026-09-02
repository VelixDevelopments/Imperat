package studio.mevera.imperat.bukkit.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import studio.mevera.imperat.bukkit.test.commands.FlagBrigadierCmd;
import studio.mevera.imperat.command.Command;
import studio.mevera.imperat.command.arguments.Argument;

import java.util.List;
import java.util.Objects;

@DisplayName("Bukkit Brigadier Flag Suggestion Tests")
class BukkitBrigadierFlagSuggestionTest {

    private BukkitImperat<BukkitCommandSource> imperat;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        ServerMock server = MockBukkit.mock();
        TestImperatPlugin plugin = MockBukkit.load(TestImperatPlugin.class);
        imperat = plugin.getImperat();
        imperat.registerCommand(new FlagBrigadierCmd());
        player = server.addPlayer("TestPlayer");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Should merge root literals with flag suggestions through Brigadier")
    void testBrigadierShowsRootFlagsAndSubcommands() {
        var suggestions = complete("flagtest ");

        // Flag-name suggestions are server-driven (core tree suggester) and
        // use the canonical single-dash forms; long `--` forms still parse.
        // Brigadier merges provider ranges (anchored to the stripped input)
        // with the request-wide [start, rawLen) range, which pads the
        // suggestion text with a space, so compare trimmed forms.
        assertEquals(6, suggestions.size());
        assertTrue(suggestions.stream().map(String::trim).toList().containsAll(
                List.of("play", "mix", "greedyflag", "multi", "-scenario", "-sc")));
    }

    @Test
    @DisplayName("Should suggest root flag values through Brigadier")
    void testBrigadierShowsRootFlagValues() {
        var suggestions = complete("flagtest -sc ");

        assertEquals(4, suggestions.size());
        assertTrue(suggestions.containsAll(List.of("kindergarten", "castle", "sandstorm", "tsunami")));
    }

    @Test
    @DisplayName("Should suggest subcommand flag names through Brigadier")
    void testBrigadierShowsSubcommandFlagNames() {
        var suggestions = complete("flagtest play ");

        assertEquals(2, suggestions.size());
        assertTrue(suggestions.containsAll(List.of("-scenario", "-sc")));
    }

    @Test
    @DisplayName("Should suggest subcommand flag values through Brigadier")
    void testBrigadierShowsSubcommandFlagValues() {
        var suggestions = complete("flagtest play -sc ");

        assertEquals(4, suggestions.size());
        assertTrue(suggestions.containsAll(List.of("kindergarten", "castle", "sandstorm", "tsunami")));
    }

    @Test
    @DisplayName("Should apply flag value suggestions without replacing the flag input")
    void testBrigadierAppliesFlagValueSuggestionAtValueOffset() {
        String input = "flagtest play --scenario ";
        Suggestion suggestion = completeSuggestions(input)
                                        .stream()
                                        .filter(candidate -> candidate.getText().equals("kindergarten"))
                                        .findFirst()
                                        .orElseThrow();

        // Value completions are anchored right after the trailing
        // space, so the inserted value does not swallow it.
        assertEquals("flagtest play --scenario kindergarten", suggestion.apply(input));
    }

    @Test
    @DisplayName("Should suggest post-argument flag names through Brigadier")
    void testBrigadierShowsFlagsAfterArguments() {
        var suggestions = complete("flagtest mix player ");

        assertEquals(2, suggestions.size());
        assertTrue(suggestions.containsAll(List.of("-scenario", "-sc")));
    }

    @Test
    @DisplayName("Should suggest post-argument flag values through Brigadier")
    void testBrigadierShowsFlagValuesAfterArguments() {
        var suggestions = complete("flagtest mix player -sc ");

        assertEquals(4, suggestions.size());
        assertTrue(suggestions.containsAll(List.of("kindergarten", "castle", "sandstorm", "tsunami")));
    }

    @Test
    @DisplayName("Should NOT suggest flag name again after it was just used in Brigadier")
    void testBrigadierDoesNotSuggestUsedFlagAgain() {
        var suggestions = complete("flagtest --sc ");

        assertFalse(suggestions.contains("--scenario"), "--scenario should not be suggested");
        assertFalse(suggestions.contains("-scenario"), "-scenario should not be suggested");
        assertFalse(suggestions.contains("-sc"), "-sc should not be suggested");
        assertFalse(suggestions.contains("--sc"), "--sc should not be suggested");
    }

    @Test
    @DisplayName("Should NOT suggest flag name after alias was used in Brigadier")
    void testBrigadierDoesNotSuggestUsedAliasFlagAgain() {
        var suggestions = complete("flagtest -sc ");

        assertFalse(suggestions.contains("--scenario"), "--scenario should not be suggested");
        assertFalse(suggestions.contains("-scenario"), "-scenario should not be suggested");
        assertFalse(suggestions.contains("-sc"), "-sc should not be suggested");
        assertFalse(suggestions.contains("--sc"), "--sc should not be suggested");
    }

    @Test
    @DisplayName("Should NOT suggest switch again after use with greedy arg in Brigadier")
    void testBrigadierDoesNotSuggestGreedySwitchAgain() {
        var suggestions = complete("flagtest greedyflag --shallow ");

        assertFalse(suggestions.contains("--shallow"), "--shallow should not be suggested, got " + suggestions);
        assertFalse(suggestions.contains("-shallow"), "-shallow should not be suggested, got " + suggestions);
    }

    @Test
    @DisplayName("Should suggest switch before use with greedy arg in Brigadier")
    void testBrigadierShowsGreedySwitchAtStart() {
        var suggestions = complete("flagtest greedyflag ");

        assertTrue(suggestions.contains("-shallow"), "expected -shallow in " + suggestions);
    }

    @Test
    @DisplayName("Should suggest the remaining switch after one switch is used in a greedy scope")
    void testBrigadierSuggestsSecondSwitchAfterFirstInGreedyScope() {
        var suggestions = complete("flagtest multi --silent ");

        assertTrue(suggestions.contains("-anon"), "expected -anon in " + suggestions);
        assertFalse(suggestions.contains("-silent"), "-silent was used, got " + suggestions);
        assertFalse(suggestions.contains("--silent"), "--silent was used, got " + suggestions);
    }

    @Test
    @DisplayName("Should suggest no switches once all are used in a greedy scope")
    void testBrigadierSuggestsNoSwitchesWhenAllUsedInGreedyScope() {
        var suggestions = complete("flagtest multi --silent -anon ");

        assertFalse(suggestions.contains("-silent"), "-silent was used, got " + suggestions);
        assertFalse(suggestions.contains("-anon"), "-anon was used, got " + suggestions);
        assertFalse(suggestions.contains("--anon"), "--anon was used, got " + suggestions);
    }

    @Test
    @DisplayName("Should suggest greedy argument values containing space correctly through Brigadier")
    void testBrigadierGreedySuggestionsWithSpace() {
        var suggestions = complete("flagtest greedyflag pluginsDir/my ");

        assertEquals(2, suggestions.size(), "Suggestions: " + suggestions);
        assertTrue(suggestions.contains("pluginsDir/my plugin/config.yml"), "expected config.yml in " + suggestions);
        assertTrue(suggestions.contains("pluginsDir/my plugin/messages.yml"), "expected messages.yml in " + suggestions);
        assertFalse(suggestions.contains("pluginsDir/itsmyconfig/"), "did not expect itsmyconfig/ in " + suggestions);
    }

    private List<String> complete(String input) {
        return completeSuggestions(input)
                       .stream()
                       .map(Suggestion::getText)
                       .toList();
    }

    private List<Suggestion> completeSuggestions(String input) {
        TestBrigadierManager manager = new TestBrigadierManager(imperat);
        Command<BukkitCommandSource> command = Objects.requireNonNull(imperat.getCommand("flagtest"));
        var dispatcher = new CommandDispatcher<PlayerMock>();
        dispatcher.getRoot().addChild(manager.parseCommandIntoNode(command));
        return dispatcher.getCompletionSuggestions(dispatcher.parse(input, player))
                       .join()
                       .getList();
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
