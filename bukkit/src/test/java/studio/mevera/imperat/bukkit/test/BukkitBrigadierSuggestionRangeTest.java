package studio.mevera.imperat.bukkit.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestion;
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
import studio.mevera.imperat.bukkit.test.commands.FlagBrigadierCmd;
import studio.mevera.imperat.bukkit.test.commands.MultiTokenBrigadierCmd;
import studio.mevera.imperat.command.Command;
import studio.mevera.imperat.command.arguments.Argument;

import java.util.List;
import java.util.Objects;

/**
 * Regression coverage for the client-visible suggestion ranges emitted by
 * the Brigadier bridge.
 *
 * <p><b>Pre-fix:</b> {@code resolveSuggestionStart} anchored empty-arg
 * completions to {@code rawInput.length()}, and Brigadier used the raw
 * input as the completion range end. When the request path appends a
 * trailing space the user has not typed, BOTH bounds landed one char past
 * the string the client renders — reproduction for {@code flagtest }
 * emitted {@code start=9 / end=9} against a client-visible length of 8.
 * Recent clients throw {@code StringIndexOutOfBoundsException} (inverted
 * substring ranges such as {@code Range [24, 19)}) while re-anchoring
 * those ranges against the shorter visible text.</p>
 *
 * <p>The fix uses a <i>hybrid</i> anchor: empty-arg completions are
 * placed at {@code rawInput.length()} (right after the trailing space
 * the client typed/appended), while non-empty partial-token completions
 * are placed on the whitespace-stripped input so the trailing space is
 * not swallowed. In both cases {@code start <= end <= rawInput.length()}
 * holds — that is what prevents the inverted substring range that the
 * original crash was about.</p>
 */
@DisplayName("Bukkit Brigadier Suggestion Range Tests")
class BukkitBrigadierSuggestionRangeTest {

    private BukkitImperat<BukkitCommandSource> imperat;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        ServerMock server = MockBukkit.mock();
        TestImperatPlugin plugin = MockBukkit.load(TestImperatPlugin.class);
        imperat = plugin.getImperat();
        imperat.registerCommand(new FlagBrigadierCmd());
        imperat.registerCommand(new MultiTokenBrigadierCmd());
        player = server.addPlayer("TestPlayer");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Trailing-space empty-arg completions stay inside the client-visible input")
    void testEmptyArgRangesStayInBounds() {
        // flagtest (flags) and mt (4-token chain) — flag values, flag names,
        // positional empty tokens, greedy positions.
        check("flagtest ");
        check("flagtest -sc ");
        check("flagtest play ");
        check("flagtest play -sc ");
        check("flagtest mix player ");
        check("flagtest multi --silent ");
        check("flagtest greedyflag ");
        check("mt ");
        check("mt 1 ");
        check("mt 1 2 ");
        check("mt 1 2 3 ");
        check("mt 1 2 3 4 ");
        check("mt 1 2 3 4 world ");
        check("mt 1 2 3 4 world overworld ");
    }

    @Test
    @DisplayName("Mid-token completions stay inside the client-visible input")
    void testMidTokenRangesStayInBounds() {
        check("flagtest -s");
        check("flagtest play -sc cas");
        check("flagtest play -sc kinde");
        check("flagtest mix e");
        check("mt 1 2 3 4 world o");
    }

    @Test
    @DisplayName("Multiple trailing spaces clamp both range bounds")
    void testMultipleTrailingSpacesStayInBounds() {
        check("flagtest  ");
        check("flagtest -sc   ");
        check("mt 1 2 3 4   ");
    }

    @Test
    @DisplayName("Completions are still surfaced after realignment")
    void testCompletionsStillSurface() {
        // Root-listing texts are expanded by Brigadier's merge with the
        // trailing space (e.g. "-sc "), so compare trimmed.
        assertContainsTrimmed(completeTexts("flagtest "), "-scenario", "-sc");
        assertContains(completeTexts("flagtest -sc "), "kindergarten", "castle", "sandstorm", "tsunami");
        assertContainsTrimmed(completeTexts("mt 1 2 3 4 "), "-mode", "-m");
    }

    @Test
    @DisplayName("Empty and slash-prefixed inputs do not produce negative offsets")
    void testNegativeAndSlashInputs() {
        check("/flagtest ");
        check("/flagtest -sc ");
        check("flagtest ");
    }

    private void check(String serverInput) {
        List<Suggestion> suggestions = complete(serverInput);
        for (Suggestion suggestion : suggestions) {
            int start = suggestion.getRange().getStart();
            int end = suggestion.getRange().getEnd();
            assertTrue(start >= 0,
                    "negative start " + start + " for input '" + serverInput + "'");
            assertTrue(start <= end,
                    "inverted range [" + start + ", " + end + ") for input '" + serverInput + "'");
            assertTrue(end <= serverInput.length(),
                    "end " + end + " > rawLen " + serverInput.length() + " for input '" + serverInput + "'");
        }
    }

    private static void assertContains(List<String> suggestions, String expected) {
        assertTrue(suggestions.contains(expected),
                "expected completion '" + expected + "' in " + suggestions);
    }

    private static void assertContains(List<String> suggestions, String... expected) {
        for (String e : expected) {
            assertContains(suggestions, e);
        }
    }

    private static void assertContainsTrimmed(List<String> suggestions, String... expected) {
        for (String e : expected) {
            assertContains(suggestions.stream().map(String::trim).toList(), e);
        }
    }

    private List<String> completeTexts(String input) {
        return complete(input).stream()
                       .map(Suggestion::getText)
                       .toList();
    }

    private List<Suggestion> complete(String input) {
        TestBrigadierManager manager = new TestBrigadierManager(imperat);
        Command<BukkitCommandSource> command = Objects.requireNonNull(
                imperat.getCommand(registrarName(input)),
                "no registered command for " + input
        );
        var dispatcher = new CommandDispatcher<PlayerMock>();
        dispatcher.getRoot().addChild(manager.parseCommandIntoNode(command));
        return dispatcher.getCompletionSuggestions(dispatcher.parse(input, player))
                       .join()
                       .getList();
    }

    private String registrarName(String input) {
        String trimmed = input.trim();
        int end = 0;
        while (end < trimmed.length() && !Character.isWhitespace(trimmed.charAt(end))) {
            end++;
        }
        String name = trimmed.substring(0, end);
        while (name.startsWith("/")) {
            name = name.substring(1);
        }
        return name;
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