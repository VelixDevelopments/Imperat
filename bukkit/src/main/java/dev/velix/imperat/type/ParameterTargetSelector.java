package dev.velix.imperat.type;

import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.command.parameters.type.BaseParameterType;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.SourceException;
import dev.velix.imperat.selector.SelectionType;
import dev.velix.imperat.selector.TargetCriteria;
import dev.velix.imperat.selector.TargetSelector;
import dev.velix.imperat.util.TypeWrap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class ParameterTargetSelector extends BaseParameterType<BukkitSource, TargetSelector> {

    private final static char PARAMETER_START = '[';
    private final static char PARAMETER_END = ']';

    private ParameterTargetSelector() {
        super(TypeWrap.of(TargetSelector.class));
    }

    @Override
    public @NotNull TargetSelector resolve(
        ExecutionContext<BukkitSource> context,
        @NotNull CommandInputStream<BukkitSource> commandInputStream
    ) throws ImperatException {

        String raw = commandInputStream.currentRaw();
        char last = raw.charAt(raw.length() - 1);

        if (commandInputStream.currentLetter() != SelectionType.MENTION_CHARACTER) {
            Player target = Bukkit.getPlayer(raw);
            if (target == null)
                return TargetSelector.of();

            return TargetSelector.of(target);
        }
        if (context.source().isConsole()) {
            throw new SourceException("Only players can use this");
        }

        SelectionType type = commandInputStream.popLetter()
            .map(SelectionType::from).orElse(SelectionType.UNKNOWN);
        //update current

        if (type == SelectionType.UNKNOWN) {
            throw new SourceException("Unknown selection type '%s'", commandInputStream.currentLetter());
        }

        List<TargetCriteria> criteria = new ArrayList<>();

        boolean parameterized = commandInputStream.popLetter().map((c) -> c == PARAMETER_START).orElse(false) && last == PARAMETER_END;
        if (parameterized) {
            String params = commandInputStream.collectBeforeFirst(PARAMETER_END);
            criteria = TargetCriteria.all(params, commandInputStream);
        }
        List<Entity> entities = type.getTargetEntities(context, commandInputStream);
        List<Entity> toCollect = new ArrayList<>();

        Predicate<Entity> entityPredicted = (entity) -> true;
        for (var singleCriteria : criteria) {
            entityPredicted = entityPredicted.and(
                (entity) -> singleCriteria.matches(context.source(), entity, toCollect)
            );
        }

        for (Entity entity : entities) {
            if (entityPredicted.test(entity)) {
                toCollect.add(entity);
            }
        }

        return TargetSelector.of(toCollect);
    }


}
