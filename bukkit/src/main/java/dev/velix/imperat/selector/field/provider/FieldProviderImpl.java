package dev.velix.imperat.selector.field.provider;

import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.context.internal.CommandInputStream;
import dev.velix.imperat.selector.field.SelectionField;
import org.jetbrains.annotations.Nullable;

final class FieldProviderImpl implements FieldProvider {

    @Override
    @SuppressWarnings("unchecked")
    public <V> @Nullable SelectionField<V> provideField(String name, CommandInputStream<BukkitSource> commandInputStream) {
        for (var field : SelectionField.ALL) {
            if (field.getName().equalsIgnoreCase(name)) {
                return (SelectionField<V>) field;
            }
        }
        return null;
    }
}
