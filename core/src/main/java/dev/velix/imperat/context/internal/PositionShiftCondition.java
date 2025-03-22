package dev.velix.imperat.context.internal;

import org.jetbrains.annotations.*;

@FunctionalInterface
interface PositionShiftCondition {
    boolean canContinue(@NotNull Cursor<?> cursor);
}