package dev.velix.context.internal.sur;

import java.util.function.IntUnaryOperator;

public enum ShiftOperation {
    RIGHT(value -> value + 1),
    
    LEFT(value -> value - 1);
    
    final IntUnaryOperator operator;
    
    ShiftOperation(IntUnaryOperator operator) {
        this.operator = operator;
    }
    
}