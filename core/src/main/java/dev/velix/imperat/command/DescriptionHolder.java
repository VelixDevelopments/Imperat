package dev.velix.imperat.command;

public interface DescriptionHolder {
    
    Description getDescription();
    
    void setDescription(Description description);
    
    default void setDescription(String description) {
        setDescription(Description.of(description));
    }
}
