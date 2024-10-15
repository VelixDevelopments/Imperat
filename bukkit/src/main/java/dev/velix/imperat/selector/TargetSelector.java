package dev.velix.imperat.selector;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public final class TargetSelector implements Iterable<Entity> {

    private final List<Entity> selectedEntities;

    private TargetSelector(List<Entity> selectedEntities) {
        this.selectedEntities = selectedEntities;
    }

    public static TargetSelector of(List<Entity> list) {
        return new TargetSelector(list);
    }

    public static TargetSelector of(Entity... list) {
        return of(List.of(list));
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public @NotNull Iterator<Entity> iterator() {
        return selectedEntities.iterator();
    }
}
