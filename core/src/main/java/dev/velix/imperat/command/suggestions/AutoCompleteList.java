package dev.velix.imperat.command.suggestions;

import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Getter
@ApiStatus.Internal
public final class AutoCompleteList implements Iterable<String> {
    
    private final List<String> results = new ArrayList<>();
    
    public void add(String result) {
        if (results.contains(result)) return;
        results.add(result);
    }
    
    public void addAll(Iterable<? extends String> results) {
        results.forEach(this::add);
    }
    
    public List<String> asList() {
        return results;
    }
    
    @Override
    public @NotNull Iterator<String> iterator() {
        return results.iterator();
    }
}
