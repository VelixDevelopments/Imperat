package dev.velix.imperat.exception.selector;

import dev.velix.imperat.exception.ParseException;

public class InvalidSelectorFieldCriteriaFormat extends ParseException {

    private final String fieldCriteriaInput;

    public InvalidSelectorFieldCriteriaFormat(String fieldCriteriaInput, String fullInput) {
        super(fullInput);
        this.fieldCriteriaInput = fieldCriteriaInput;
    }

    public String getFieldCriteriaInput() {
        return fieldCriteriaInput;
    }
}
