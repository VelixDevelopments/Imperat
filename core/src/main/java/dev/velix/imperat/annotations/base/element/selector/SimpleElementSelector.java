package dev.velix.imperat.annotations.base.element.selector;

import dev.velix.imperat.annotations.base.element.ParseElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

final class SimpleElementSelector<E extends ParseElement<?>> implements ElementSelector<E> {
    
    private final List<Rule<E>> rules = new ArrayList<>();
    
    SimpleElementSelector() {
    
    }
    
    @Override
    public @NotNull List<Rule<E>> getRules() {
        return rules;
    }
}
