package dev.velix.imperat.selector.field.filters;

import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.SourceException;
import dev.velix.imperat.selector.EntityCondition;
import dev.velix.imperat.util.TypeWrap;
import org.bukkit.GameMode;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.*;

import java.util.Arrays;

final class GamemodeField extends PredicateField<GameMode> {

    GamemodeField(String name) {
        super(name, TypeWrap.of(GameMode.class));
        Arrays.stream(GameMode.values())
            .map(GameMode::name)
            .map(String::toLowerCase)
            .forEach(suggestions::add);
    }

    @Override
    protected @NotNull EntityCondition getCondition(GameMode value, CommandInputStream<BukkitSource> commandInputStream) {
        return ((sender, entity) -> {
            if (!(entity instanceof HumanEntity humanEntity)) return false;
            return humanEntity.getGameMode() == value;
        });
    }

    /**
     * Parses the given string representation of the value and converts it into the field's value type.
     *
     * @param value the string representation of the value to be parsed
     * @return the parsed value of the field's type
     * @throws ImperatException if the parsing fails
     */
    @Override
    public GameMode parseFieldValue(String value) throws ImperatException {
        try {
            return GameMode.valueOf(value);
        } catch (EnumConstantNotPresentException ex) {
            throw new SourceException("Unknown gamemode '%s'", value);
        }
    }
}
