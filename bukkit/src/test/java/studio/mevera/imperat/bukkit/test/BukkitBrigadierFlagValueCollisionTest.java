package studio.mevera.imperat.bukkit.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
import studio.mevera.imperat.bukkit.test.commands.FlagCollisionBrigadierCmd;
import studio.mevera.imperat.command.Command;
import studio.mevera.imperat.command.arguments.Argument;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@DisplayName("Bukkit Brigadier Flag Value Collision Tests")
class BukkitBrigadierFlagValueCollisionTest {

    private ServerMock server;
    private TestImperatPlugin plugin;
    private BukkitImperat imperat;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestImperatPlugin.class);
        imperat = plugin.getImperat();
        imperat.registerCommand(new FlagCollisionBrigadierCmd());
        player = server.addPlayer("TestPlayer");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Should still suggest a flag whose form was only used as another flag's VALUE")
    void testFlagValueMatchingAnotherFlagFormDoesNotSuppress() {
        // `-sc` here is the VALUE of the `-tag` flag, not a use of the
        // `sc` switch — the switch must remain suggestible.
        var suggestions = complete("ff -tag -sc ");

        assertTrue(suggestions.contains("-sc"), "expected -sc in " + suggestions);
        assertFalse(suggestions.contains("-tag"), "-tag was used, got " + suggestions);
        assertFalse(suggestions.contains("--tag"), "--tag was used, got " + suggestions);
    }

    @Test
    @DisplayName("Should match suggestion prefixes with Locale.ROOT case folding")
    void testPrefixFilterIsLocaleIndependent() {
        Locale previous = Locale.getDefault();
        Locale.setDefault(Locale.forLanguageTag("tr-TR"));
        try {
            // Turkish locale: "IstanbulPark".toLowerCase() is "ıstanbulpark"
            // (dotless ı), which does NOT start with "ist" — a default-locale
            // toLowerCase drops the suggestion entirely.
            var suggestions = complete("ff ist");

            assertTrue(suggestions.contains("IstanbulPark"),
                    "expected IstanbulPark in " + suggestions);
        } finally {
            Locale.setDefault(previous);
        }
    }

    private List<String> complete(String input) {
        TestBrigadierManager manager = new TestBrigadierManager(imperat);
        Command<BukkitCommandSource> command = Objects.requireNonNull(imperat.getCommand("ff"));
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
