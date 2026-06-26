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
import studio.mevera.imperat.PermissiveStringArgumentType;
import studio.mevera.imperat.backend.modern.argument.PaperNativeArgumentType;
import studio.mevera.imperat.command.Command;
import studio.mevera.imperat.command.arguments.Argument;
import studio.mevera.imperat.command.arguments.FlagArgument;

import java.util.ArrayList;
import java.util.Collection;
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

    @Override
    protected @NotNull ArgumentType<?> getFlagValueArgumentType(
            @NotNull FlagArgument<S> flag
    ) {
        var inputType = flag.flagData().inputType();
        if (inputType == null) {
            return wrapForPaper(super.getFlagValueArgumentType(flag));
        }
        ArgumentType<?> nativeType = paperNativeOf(inputType);
        if (nativeType != null) {
            return nativeType;
        }
        return wrapForPaper(super.getFlagValueArgumentType(flag));
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
     * Flag-value variant of {@link #createSuggestionProvider} — same
     * native-delegation rule, applied to the flag's input type.
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected <BS> @Nullable SuggestionProvider<BS> createNativeFlagValueSuggester(
            @NotNull FlagArgument<S> flag
    ) {
        var inputType = flag.flagData().inputType();
        if (inputType == null) {
            return null;
        }
        ArgumentType<?> nativeType = paperNativeOf(inputType);
        if (nativeType == null) {
            return null;
        }
        return ((ArgumentType) nativeType)::listSuggestions;
    }

    /**
     * Modern Paper opts out of the inline-flag catch-all sibling.
     *
     * <p>Two failed paths walked first:
     * <ul>
     *   <li>Raw {@link studio.mevera.imperat.InlineFlagArgumentType}: rejected by Paper's
     *       {@code Commands} registrar with
     *       "Custom unknown argument type was passed, should be wrapped
     *       inside a CustomArgumentType".</li>
     *   <li>Wrapped via {@code CustomArgumentType} with
     *       {@code greedyString} as native: registers, but the greedy
     *       native sent to the client confuses Brigadier's client-side
     *       tree-walk for completions — sibling nodes never collect
     *       suggestions because the client thinks {@code <flag>}
     *       consumes the rest of input.</li>
     * </ul></p>
     *
     * <p>No vanilla native type accepts {@code =} in single-token form
     * AND leaves siblings reachable for client-side completion. Skip the
     * node here. Inline {@code -flag=value} renders red on modern Paper,
     * but completions still flow via the
     * {@link BaseBrigadierManager}-level positional-suggester wrapper
     * (delegates to the Imperat tree), and execution succeeds via the
     * {@code UnknownCommandEvent} fallback in {@code BukkitImperat}.</p>
     */
    @Override
    protected @Nullable ArgumentType<?> inlineFlagArgumentType() {
        return null;
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
     * Wraps a {@link PermissiveStringArgumentType} in Paper's
     * {@link CustomArgumentType} so the {@code ApiMirrorRootNode}
     * accepts it. Paper's modern Brigadier registrar rejects raw
     * custom {@link ArgumentType} implementations — they must be
     * wrapped with a native type for client-side tree sync.
     * Non-permissive types pass through unchanged.
     */
    private static ArgumentType<?> wrapForPaper(ArgumentType<?> type) {
        if (type instanceof PermissiveStringArgumentType pst) {
            return new CustomArgumentType<String, String>() {
                @Override
                public @NonNull String parse(@NonNull StringReader reader) throws CommandSyntaxException {
                    return pst.parse(reader);
                }
                @Override
                public @NonNull ArgumentType<String> getNativeType() {
                    return StringArgumentType.string();
                }
            };
        }
        return type;
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
