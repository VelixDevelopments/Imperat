package dev.velix.imperat.exception;

import dev.velix.imperat.ConfigBuilder;
import dev.velix.imperat.annotations.Greedy;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.verification.UsageVerifier;

/**
 * Happens when the {@link CommandUsage} being registered breaks the rules of a valid usage
 * These rules are defined by the {@link UsageVerifier}.
 * you can implement your own {@link UsageVerifier} and register it
 * for imperat's config to use by calling {@link ConfigBuilder#usageVerifier(UsageVerifier)}
 *
 * <p>
 * The default {@link UsageVerifier} has the following rules:
 * Rule 1: At-MOST, only one greedy {@link CommandParameter} is allowed in a {@link CommandUsage}
 * Rule 2: The position of a greedy {@link CommandParameter} MUST be the LAST.
 * </p>
 *
 * <p>
 * Note: A greedy argument can be annotated with {@link Greedy}
 * or its type is either of [Array, Collection, Map]
 * </p>
 */
public final class UsageRegistrationException extends RuntimeException {
    
    /**
     * Constructs a new UsageRegistrationException for the given command and usage that
     * violates the usage rules.
     *
     * @param <S> the type of the command source
     * @param command the command that contains the invalid usage
     * @param usage the command usage that violates the rules
     *
     * @see UsageVerifier for rules about valid command usages
     * @see ConfigBuilder#usageVerifier(UsageVerifier) for registering custom verifiers
     */
    public <S extends Source> UsageRegistrationException(
        final Command<S> command,
        final CommandUsage<S> usage
    ) {
        super(
            String.format("Invalid command usage: '%s'", CommandUsage.format(command, usage))
        );
    }

}
