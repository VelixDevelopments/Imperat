package dev.velix.imperat.context.internal.sur;

public enum ShiftTarget {
    
    RAW_ONLY((pos, maxParam, maxRaw) -> pos.getRaw() < maxRaw),
    
    PARAMETER_ONLY((pos, maxParam, maxRaw) -> pos.getParameter() < maxParam),
    
    ALL((pos, maxRaw, maxParameter) ->
            pos.getRaw() < maxRaw && pos.getParameter() < maxParameter);
    
    private final PositionShiftCondition canContinueCheck;
    
    ShiftTarget(PositionShiftCondition canContinueCheck) {
        this.canContinueCheck = canContinueCheck;
    }
    
    boolean canContinue(Cursor<?> cursor, int maxParam, int maxRaw) {
        return canContinueCheck.canContinue(cursor, maxParam, maxRaw);
    }
    
}