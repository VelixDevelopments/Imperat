package dev.velix.imperat.help;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.Source;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@Accessors(fluent = true)
public final class HyphenContent<S extends Source> {
    private final Command<S> command;
    private final int currentPage, maxPages;
    
    public static <S extends Source> HyphenContent<S> of(Command<S> cmd, int currentPage, int maxPages) {
        return new HyphenContent<>(cmd, currentPage, maxPages);
    }
}
