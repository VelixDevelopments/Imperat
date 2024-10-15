package dev.velix.imperat.selector;

import dev.velix.imperat.context.Source;
import org.bukkit.entity.Entity;

import java.util.List;

@FunctionalInterface
public interface EntityCondition {

    boolean test(Source sender, Entity entity, List<Entity> collectedEntities);
}
