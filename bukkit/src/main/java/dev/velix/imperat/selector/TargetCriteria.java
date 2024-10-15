package dev.velix.imperat.selector;

import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.SourceException;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a criteria
 */
public interface TargetCriteria {

    EntityCondition toCondition();

    default boolean matches(Source source, Entity entity, List<Entity> collectedEntities) {
        return toCondition().test(source, entity, collectedEntities);
    }

    static <S extends Source> TargetCriteria of(TargetField field, String value, CommandInputStream<S> inputStream) {
        return new SimpleCriteria(field, value, inputStream);
    }

    static <S extends Source> TargetCriteria from(String str, CommandInputStream<S> inputStream) throws ImperatException {
        String[] split = str.split(String.valueOf(TargetField.VALUE_EQUALS));
        if (split.length != 2) {
            throw new SourceException("Invalid field-criteria format '%s'", str);
        }
        String field = split[0], value = split[1];

        try {
            return new SimpleCriteria(TargetField.valueOf(field), value, inputStream);
        } catch (EnumConstantNotPresentException ex) {
            throw new SourceException("Unknown field '%s'", field);
        }

    }

    static <S extends Source, E extends Entity> List<TargetCriteria> all(String paramsString, CommandInputStream<S> inputStream) throws ImperatException {
        String[] params = paramsString.split(String.valueOf(TargetField.SEPARATOR));
        if (params.length == 0) {
            return Collections.singletonList(from(paramsString, inputStream));
        }
        List<TargetCriteria> list = new ArrayList<>();
        for (String str : params) {
            TargetCriteria from = TargetCriteria.from(str, inputStream);
            list.add(from);
        }
        return list;
    }
}
