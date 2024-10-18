package dev.velix.imperat.selector.field.filters;

import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.selector.EntityCondition;
import dev.velix.imperat.selector.field.AbstractField;
import dev.velix.imperat.util.TypeWrap;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * The PredicateField interface extends the SelectionField interface to add filtering
 * functionality based on a specific value. Implementing this interface allows
 * defining a condition that can be used to filter entities.
 *
 * @param <V> The type of the value that the filter field handles.
 */
public abstract class PredicateField<V> extends AbstractField<V> implements PredicateFields {

    /**
     * Constructs an AbstractField instance with the specified name and type.
     *
     * @param name The name of the selection field.
     * @param type The type information of the value that this field handles, wrapped in a TypeWrap object.
     */
    protected PredicateField(String name, TypeWrap<V> type) {
        super(name, type);
    }

    /**
     * Generates an {@link EntityCondition} based on the given value and command input stream.
     * This method is intended to be implemented by subclasses to provide specific
     * filtering conditions for entities.
     *
     * @param value              The value used to generate the condition.
     * @param commandInputStream The stream providing command input data.
     * @return The condition that will be used to filter entities based on the value and input data.
     */
    @NotNull
    protected abstract EntityCondition getCondition(V value, CommandInputStream<BukkitSource> commandInputStream);

    public final boolean isApplicable(BukkitSource sender, Entity entity, V value, CommandInputStream<BukkitSource> commandInputStream) throws ImperatException {
        EntityCondition condition = getCondition(value, commandInputStream);
        return condition.test(sender, entity);
    }
}
