package dev.velix.imperat.exception.parse;

import dev.velix.imperat.exception.ParseException;

import java.util.List;

public class WordOutOfRestrictionsException extends ParseException {

    private final List<String> restrictions;
    public WordOutOfRestrictionsException(String input, List<String> restrictions) {
        super(input);
        this.restrictions = restrictions;
    }

    public List<String> getRestrictions() {
        return restrictions;
    }

}
