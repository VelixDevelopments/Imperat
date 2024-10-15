package dev.velix.imperat.selector;

import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.CommandInputStream;
import org.bukkit.command.CommandSender;

final class SimpleCriteria implements TargetCriteria {

    private final EntityCondition condition;

    <S extends Source> SimpleCriteria(TargetField field, String value, CommandInputStream<S> inputStream) {
        condition = (sender, entity, collectedEntities) -> {
            try {
                return field.handle(sender.as(CommandSender.class), entity, collectedEntities, value, inputStream);
            } catch (dev.velix.imperat.exception.ImperatException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override
    public EntityCondition toCondition() {
        return condition;
    }
}
