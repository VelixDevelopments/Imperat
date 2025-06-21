package dev.velix.imperat.command.flags;

import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.context.FlagData;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.exception.UnknownFlagException;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;

/**
 * A class that extracts flags from a single string/argument.
 * @param <S> the type of the command source
 */
@ApiStatus.AvailableSince("1.9.6")
public sealed interface FlagExtractor<S extends Source> permits FlagExtractorImpl {

    /**
     * Inserts a flag during into the trie.
     * May be useful if it's necessary to insert a flag during runtime.
     * @param flagData the {@link FlagData} to insert
     */
    void insertFlag(FlagData<S> flagData);

    /**
     * Extracts all flags used from a single string with no spaces.
     * @param rawInput the raw input of an argument
     * @return the extracted {@link FlagData} for flags.
     */
    Set<FlagData<S>> extract(String rawInput) throws UnknownFlagException;

    static <S extends Source> FlagExtractor<S> createNative(CommandUsage<S> usage) {
        return new FlagExtractorImpl<>(usage);
    }
}
