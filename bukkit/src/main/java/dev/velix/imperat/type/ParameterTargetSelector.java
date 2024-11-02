package dev.velix.imperat.type;

import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.type.BaseParameterType;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.SuggestionContext;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.SourceException;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.selector.EntityCondition;
import dev.velix.imperat.selector.SelectionParameterInput;
import dev.velix.imperat.selector.SelectionType;
import dev.velix.imperat.selector.TargetSelector;
import dev.velix.imperat.selector.field.filters.PredicateField;
import dev.velix.imperat.selector.field.operators.OperatorField;
import dev.velix.imperat.util.TypeWrap;
import dev.velix.imperat.util.Version;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ParameterTargetSelector extends BaseParameterType<BukkitSource, TargetSelector> {

    private final static char PARAMETER_START = '[';
    private final static char PARAMETER_END = ']';

    private final SuggestionResolver<BukkitSource> suggestionResolver;

    public ParameterTargetSelector() {
        super(TypeWrap.of(TargetSelector.class));
        SelectionType.TYPES.stream()
            .filter(type -> type != SelectionType.UNKNOWN)
            .map(SelectionType::id)
            .forEach((id) -> suggestions.add(SelectionType.MENTION_CHARACTER + id));
        suggestionResolver = new TargetSelectorSuggestionResolver();
    }

    @Override
    public @NotNull TargetSelector resolve(
        ExecutionContext<BukkitSource> context,
        @NotNull CommandInputStream<BukkitSource> commandInputStream
    ) throws ImperatException {

        String raw = commandInputStream.currentRaw().orElse(null);
        if (raw == null)
            return TargetSelector.empty();

        if (Version.isOrOver(13)) {
            SelectionType type = commandInputStream.popLetter()
                .map((s) -> SelectionType.from(String.valueOf(s))).orElse(SelectionType.UNKNOWN);
            return TargetSelector.of(
                type,
                Bukkit.selectEntities(context.source().origin(), raw)
            );
        }

        char last = raw.charAt(raw.length() - 1);

        if (commandInputStream.currentLetter()
            .filter((c) -> String.valueOf(c).equalsIgnoreCase(SelectionType.MENTION_CHARACTER)).isEmpty()) {
            Player target = Bukkit.getPlayer(raw);
            if (target == null)
                return TargetSelector.empty();

            return TargetSelector.of(SelectionType.UNKNOWN, target);
        }

        /*if (context.source().isConsole()) {
            throw new SourceException("Only players can use this");
        }*/


        SelectionType type = commandInputStream.popLetter()
            .map((s) -> SelectionType.from(String.valueOf(s))).orElse(SelectionType.UNKNOWN);
        //update current

        if (type == SelectionType.UNKNOWN) {
            throw new SourceException("Unknown selection type '%s'", commandInputStream.currentLetter().orElseThrow());
        }

        List<SelectionParameterInput<?>> inputParameters = new ArrayList<>();

        boolean parameterized = commandInputStream.popLetter().map((c) -> c == PARAMETER_START).orElse(false) && last == PARAMETER_END;
        if (parameterized) {
            commandInputStream.skipLetter();

            String params = commandInputStream.collectBeforeFirst(PARAMETER_END);
            inputParameters = SelectionParameterInput.parseAll(params, commandInputStream);
        }

        List<Entity> entities = type.getTargetEntities(context, commandInputStream);
        List<Entity> selected = new ArrayList<>();

        EntityCondition entityPredicted = getEntityPredicate(commandInputStream, inputParameters);
        for (Entity entity : entities) {
            if (entityPredicted.test(context.source(), entity)) {
                selected.add(entity);
            }
        }
        operateFields(inputParameters, selected);

        return TargetSelector.of(type, selected);
    }

    @SuppressWarnings("unchecked")
    private static @NotNull <V> EntityCondition getEntityPredicate(@NotNull CommandInputStream<BukkitSource> commandInputStream, List<SelectionParameterInput<?>> inputParameters) {
        EntityCondition entityPredicted = (sender, entity) -> true;
        for (var input : inputParameters) {
            if (!(input.getField() instanceof PredicateField<?>)) continue;
            PredicateField<V> predicateField = (PredicateField<V>) input.getField();
            entityPredicted = entityPredicted.and(
                (sender, entity) -> predicateField.isApplicable(sender, entity, (V) input.getValue(), commandInputStream)
            );

        }
        return entityPredicted;
    }

    @SuppressWarnings("unchecked")
    private static <V> void operateFields(List<SelectionParameterInput<?>> inputParameters, List<Entity> selected) {
        for (var input : inputParameters) {
            if (input.getField() instanceof OperatorField<?>) {
                OperatorField<V> operatorField = (OperatorField<V>) input.getField();
                operatorField.operate((V) input.getValue(), selected);
            }
        }
    }

    /**
     * Returns the suggestion resolver associated with this parameter type.
     *
     * @return the suggestion resolver for generating suggestions based on the parameter type.
     */
    @Override
    public SuggestionResolver<BukkitSource> getSuggestionResolver() {
        return suggestionResolver;
    }

    private final class TargetSelectorSuggestionResolver implements SuggestionResolver<BukkitSource> {

        /**
         * @param context   the context for suggestions
         * @param parameter the parameter of the value to complete
         * @return the auto-completed suggestions of the current argument
         */
        @Override
        public Collection<String> autoComplete(
            SuggestionContext<BukkitSource> context,
            CommandParameter<BukkitSource> parameter
        ) {
            //ImperatDebugger.debug("SUGGESTING for TargetSelector");
            List<String> completions = new ArrayList<>(suggestions);
            Bukkit.getOnlinePlayers().stream().
                map(Player::getName).forEach(completions::add);
            return completions;
        }
    }

}
