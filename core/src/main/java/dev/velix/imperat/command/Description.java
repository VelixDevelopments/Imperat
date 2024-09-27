package dev.velix.imperat.command;

public final class Description {
    
    public final static Description EMPTY = Description.of("N/A");
    private final String value;
    
    Description(String value) {
        this.value = value;
    }
    
    public static Description of(String value) {
        return new Description(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
