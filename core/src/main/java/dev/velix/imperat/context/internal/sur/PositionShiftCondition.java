package dev.velix.imperat.context.internal.sur;

@FunctionalInterface
interface PositionShiftCondition {
    boolean canContinue(Pivot pivot, int maxRaw, int maxParameter);
}