package dev.velix.imperat.type;

import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.type.BaseParameterType;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.SuggestionContext;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.util.TypeWrap;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;

public class ParameterColorBungee extends BaseParameterType<BukkitSource, ChatColor> {

    private final ColorSuggestionResolver SUGGESTION_RESOLVER = new ColorSuggestionResolver();

    public ParameterColorBungee() {
        super(TypeWrap.of(ChatColor.class));
    }

    @Override
    public @Nullable ChatColor resolve(ExecutionContext<BukkitSource> context, @NotNull CommandInputStream<BukkitSource> commandInputStream) {
        final String raw = commandInputStream.currentRaw().orElse(null);
        return raw == null ? null : ChatColor.valueOf(raw);
    }

    @Override
    public SuggestionResolver<BukkitSource> getSuggestionResolver() {
        return SUGGESTION_RESOLVER;
    }

    private final static class ColorSuggestionResolver implements SuggestionResolver<BukkitSource> {

        private final Collection<String> colors = Arrays.stream(ChatColor.values()).map(ChatColor::name).toList();

        /**
         * @param context   the context for suggestions
         * @param parameter the parameter of the value to complete
         * @return the auto-completed suggestions of the current argument
         */
        @Override
        public Collection<String> autoComplete(SuggestionContext<BukkitSource> context, CommandParameter<BukkitSource> parameter) {
            return colors;
        }
    }

}
