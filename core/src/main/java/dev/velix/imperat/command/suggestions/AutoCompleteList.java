package dev.velix.imperat.command.suggestions;

import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@ApiStatus.Internal
final class AutoCompleteList {
    
    private final Set<String> results = new LinkedHashSet<>();
    
    public void add(String result) {
        results.add(result);
    }
    
    public void addAll(List<String> results) {
        this.results.addAll(results);
    }
    
}
