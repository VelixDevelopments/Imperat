package studio.mevera.imperat.backend.modern;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import studio.mevera.imperat.BaseBrigadierManager;
import studio.mevera.imperat.BukkitCommandSource;
import studio.mevera.imperat.BukkitImperat;
import studio.mevera.imperat.FlagTokenArgumentType;
import studio.mevera.imperat.PermissiveStringArgumentType;
import studio.mevera.imperat.backend.modern.argument.PaperNativeArgumentType;
import studio.mevera.imperat.command.Command;
import studio.mevera.imperat.command.arguments.Argument;
import studio.mevera.imperat.command.arguments.FlagArgument;

import java.util.ArrayList;
import java.util.List;

/**
 * Modern-Paper Brigadier registrar. Extends the framework's
 * {@link BaseBrigadierManager} (projection-driven) to produce a
 * Brigadier {@code LiteralCommandNode<CommandSourceStack>} per Imperat
 * command, then hands it to Paper's {@link Commands} registrar through
 * the {@code COMMANDS} lifecycle event.
 *
 * <p>Argument-type lookup goes through the unified
 * {@link PaperNativeArgumentType} SPI — covers both the
 * {@code PaperBukkitArgumentType} wrappers ({@code World},
 * {@code GameMode}, {@code ItemStack}, etc.) and Imperat-side parsers
 * with native rendering ({@code PaperTargetSelectorArgument}). Imperat
 * types without a native counterpart fall back to plain string.</p>
 *
 * @since 4.0.0 (Paper module)
 */
@SuppressWarnings("UnstableApiUsage")
public final class ModernPaperBrigadierManager<S extends BukkitCommandSource> extends BaseBrigadierManager<S> {

    private final BukkitImperat<S> bukkitImperat;

    public ModernPaperBrigadierManager(@NotNull BukkitImperat<S> imperat) {
        super(imperat);
        this.bukkitImperat = imperat;
    }

    @Override
    public S wrapCommandSource(Object commandSource) {
        return bukkitImperat.wrapSender(commandSource);
    }

    @Override
    public @NotNull ArgumentType<?> getArgumentType(
            @NotNull Argument<S> imperatArgument
    ) {
        ArgumentType<?> nativeType = paperNativeOf(imperatArgument.type());
        return nativeType != null ? nativeType : getStringArgType(imperatArgument);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected @NotNull ArgumentType<String> getStringArgType(
            @NotNull Argument<S> parameter
    ) {
        ArgumentType<String> type = super.getStringArgType(parameter);
        if (type instanceof PermissiveStringArgumentType) {
            return (ArgumentType<String>) wrapForPaper(type);
        }
        return type;
    }

    /**
     * Wraps the platform-agnostic {@link FlagTokenArgumentType} in Paper's
     * {@link CustomArgumentType} (native {@code string()}) so the modern
     * registrar accepts it. Bare flag forms ({@code -sc}, {@code --scenario})
     * fit {@code string()}'s unquoted charset and stay green client-side;
     * inline {@code -name=value} still renders red on modern Paper because
     * the native charset excludes {@code =} (unchanged from before — the
     * old inline catch-all was skipped here entirely), while completions
     * and execution remain server-driven and correct.
     */
    @Override
    protected @NotNull ArgumentType<?> flagTokenArgumentType() {
        return wrapStringLike(new FlagTokenArgumentType(), StringArgumentType.string());
    }

    @Override
    protected @NotNull ArgumentType<?> flagValueArgumentType() {
        return wrapStringLike(new PermissiveStringArgumentType(), StringArgumentType.string());
    }

    /**
     * Routes positional-arg tab completion to the Paper-native type's
     * {@code listSuggestions} when the Imperat-side type implements
     * {@link PaperNativeArgumentType}. Without this override, the base
     * class would emit Imperat-tree-derived completions; the native
     * parser already produces richer client-aware ones (selector char
     * menu, filter keys, dimension list, etc.).
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected @NotNull <BS> SuggestionProvider<BS> createSuggestionProvider(
            Command<S> command,
            Argument<S> parameter
    ) {
        ArgumentType<?> nativeType = paperNativeOf(parameter.type());
        if (nativeType != null) {
            return ((ArgumentType) nativeType)::listSuggestions;
        }
        return super.createSuggestionProvider(command, parameter);
    }

    /**
     * Builds the Brigadier node tree for {@code command} and registers it
     * with Paper's {@link Commands} registrar (captured during the
     * {@code COMMANDS} lifecycle event).
     */
    public void register(@NotNull Commands registrar, @NotNull Command<S> command) {
        LiteralCommandNode<CommandSourceStack> node = this.parseCommandIntoNode(command);
        command.getDescription();
        String description = command.getDescription().getValueOrElse("");
        List<String> aliases = new ArrayList<>(command.aliases());
        registrar.register(node, description.isEmpty() ? null : description, aliases);
    }

    /**
     * Wraps a custom string-shaped {@link ArgumentType} in Paper's
     * {@link CustomArgumentType} so the {@code ApiMirrorRootNode}
     * accepts it. Paper's modern Brigadier registrar rejects raw
     * custom {@link ArgumentType} implementations — they must be
     * wrapped with a native type for client-side tree sync.
     * Non-permissive types pass through unchanged.
     */
    private static ArgumentType<?> wrapForPaper(ArgumentType<?> type) {
        if (type instanceof PermissiveStringArgumentType pst) {
            return wrapStringLike(pst, StringArgumentType.string());
        }
        return type;
    }

    private static ArgumentType<String> wrapStringLike(ArgumentType<String> delegate, ArgumentType<String> nativeType) {
        return new CustomArgumentType<String, String>() {
            @Override
            public @NonNull String parse(@NonNull StringReader reader) throws CommandSyntaxException {
                return delegate.parse(reader);
            }
            @Override
            public @NonNull ArgumentType<String> getNativeType() {
                return nativeType;
            }
        };
    }

    /**
     * Returns the Paper Brigadier-native type backing {@code imperatType},
     * or {@code null} if the type doesn't expose one. Single source of
     * truth used by every native-aware override on this class:
     * {@link #getArgumentType(Argument)}, {@link #getFlagValueArgumentType(FlagArgument)},
     * {@link #createSuggestionProvider(Command, Argument)}, and
     * {@link #createNativeFlagValueSuggester(FlagArgument)}.
     */
    private @Nullable ArgumentType<?> paperNativeOf(
            studio.mevera.imperat.command.arguments.type.ArgumentType<S, ?> imperatType
    ) {
        return imperatType instanceof PaperNativeArgumentType paperNative
                       ? paperNative.nativeType()
                       : null;
    }
}
