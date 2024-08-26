package dev.velix.imperat.command.parameters;

import lombok.Getter;

@Getter
public final class NumericRange{
	
	private final double min, max;
	NumericRange(double min, double max ) {
		this.min = min;
		this.max = max;
	}
	
	public static NumericRange of(double min, double max) {
		return new NumericRange(min, max);
	}
	
	public static NumericRange min(double min) {
		return new NumericRange(min, Double.MAX_VALUE);
	}
	
	public static NumericRange max(double max) {
		return new NumericRange(Double.MIN_VALUE, max);
	}
	
	
}
