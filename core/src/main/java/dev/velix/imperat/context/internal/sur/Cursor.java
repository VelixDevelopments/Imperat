package dev.velix.imperat.context.internal.sur;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.IntUnaryOperator;

@Data
@AllArgsConstructor
public final class Cursor {
    
    int parameter, raw;
    
    public void shift(ShiftTarget shift, IntUnaryOperator operator) {
        if (shift == ShiftTarget.RAW_ONLY)
            this.raw = operator.applyAsInt(raw);
        else if (shift == ShiftTarget.PARAMETER_ONLY)
            this.parameter = operator.applyAsInt(parameter);
        else {
            this.raw = operator.applyAsInt(raw);
            this.parameter = operator.applyAsInt(parameter);
        }
        //System.out.println("r=" + raw + ", p=" +parameter);
    }
    
    public void shift(ShiftTarget target, ShiftOperation operation) {
        shift(target, operation.operator);
    }
    
    public boolean canContinue(ShiftTarget target,
                               List<CommandParameter> parameters,
                               ArgumentQueue queue) {
        return target.canContinue(this, parameters.size(), queue.size());
    }
    
    
    public boolean isLast(ShiftTarget shiftTarget, int maxParams, int maxRaws) {
        if (shiftTarget == ShiftTarget.PARAMETER_ONLY)
            return parameter == maxParams - 1;
        else if (shiftTarget == ShiftTarget.RAW_ONLY)
            return raw == maxRaws - 1;
        else
            return parameter == maxParams - 1 && raw == maxRaws - 1;
    }
    
    public boolean isLast(ShiftTarget shiftTarget, List<CommandParameter> params, ArgumentQueue raws) {
        return isLast(shiftTarget, params.size(), raws.size());
    }
    
    
    public @Nullable CommandParameter peekParameter(List<CommandParameter> parameters) {
        return parameters.get(this.parameter);
    }
    
    public @Nullable String peekRaw(ArgumentQueue raws) {
        try {
            return raws.get(raw);
        } catch (Exception ex) {
            return null;
        }
    }
    
    public @Nullable String nextRaw(ArgumentQueue queue) {
        shift(ShiftTarget.RAW_ONLY, ShiftOperation.RIGHT);
        return peekRaw(queue);
    }
    
    public <S extends Source> @Nullable String nextRaw(Context<S> context) {
        return nextRaw(context.getArguments());
    }
    
}
