package dev.velix.imperat.help;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.Source;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public record HyphenContent<S extends Source>(Command<S> command, int currentPage, int maxPages) {
    public static <S extends Source> HyphenContent<S> of(Command<S> cmd, int currentPage, int maxPages) {
        return new HyphenContent<>(cmd, currentPage, maxPages);
    }
}
