package dev.velix.imperat.context.internal.sur;

@FunctionalInterface
interface PositionShiftCondition {
    boolean canContinue(Cursor cursor, int maxRaw, int maxParameter);
}