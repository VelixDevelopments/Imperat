package dev.velix.imperat.selector.field;

import dev.velix.imperat.selector.field.filters.PredicateField;
import dev.velix.imperat.selector.field.operators.OperatorField;
import dev.velix.imperat.util.BukkitUtil;

import java.util.HashSet;
import java.util.Set;

//minestom style ;D
interface SelectionFields {

    //TODO add constants for each type of field

    Set<SelectionField<?>> ALL = BukkitUtil.mergedSet(
        new HashSet<>(PredicateField.ALL_PREDICATES),
        new HashSet<>(OperatorField.ALL_OPERATORS),
        HashSet::new
    );

}
