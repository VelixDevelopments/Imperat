package dev.velix.imperat.placeholders;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.util.Registry;

public final class PlaceholderRegistry<S extends Source> extends Registry<String, Placeholder<S>> {
    
    private final Imperat<S> imperat;
    
    PlaceholderRegistry(Imperat<S> imperat) {
        this.imperat = imperat;
    }
    
    public static <S extends Source> PlaceholderRegistry<S> createDefault(Imperat<S> imperat) {
        return new PlaceholderRegistry<>(imperat);
    }
    
    public String resolvedString(String input) {
        
        String result = input;
        for (var placeHolder : getAll()) {
            
            if (placeHolder.isUsedIn(result)) {
                String id = placeHolder.id();
                result = result.replaceAll(id, placeHolder.resolveInput(id, imperat));
            }
            
        }
        return result;
    }
    
    
}
