package dev.velix.imperat.context.internal;

enum ShiftTarget {

    RAW_ONLY((pos) -> pos.raw < pos.maxRaws()),

    PARAMETER_ONLY((pos) -> pos.parameter < pos.maxParameters()),

    ALL((pos) ->
        pos.raw < pos.maxRaws() && pos.parameter < pos.maxParameters());

    private final PositionShiftCondition canContinueCheck;

    ShiftTarget(PositionShiftCondition canContinueCheck) {
        this.canContinueCheck = canContinueCheck;
    }

    boolean canContinue(Cursor<?> cursor) {
        return canContinueCheck.canContinue(cursor);
    }

}