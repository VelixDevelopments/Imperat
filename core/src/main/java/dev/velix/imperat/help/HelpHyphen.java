package dev.velix.imperat.help;

import dev.velix.imperat.context.Source;

@FunctionalInterface
public interface HelpHyphen<S extends Source> {
    
    String value(HyphenContent<S> content);
}
