package dev.velix.imperat.context.internal;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
interface PositionShiftCondition {
    boolean canContinue(@NotNull Cursor<?> cursor);
}