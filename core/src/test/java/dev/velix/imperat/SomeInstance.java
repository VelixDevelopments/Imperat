package dev.velix.imperat;

public class SomeInstance {
    private final String value;
    
    
    public SomeInstance(String value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return value;
    }
    
}
