package dev.velix.imperat.command.parameters;

import dev.velix.imperat.annotations.parameters.AnnotatedParameter;
import dev.velix.imperat.annotations.parameters.NumericParameterDecorator;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.Description;
import dev.velix.imperat.context.CommandFlag;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.supplier.OptionalValueSupplier;
import dev.velix.imperat.supplier.defaults.BooleanValueSupplier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Represents the command parameter required
 * by the usage of the command itself
 */
@ApiStatus.AvailableSince("1.0.0")
@SuppressWarnings("unused")

public interface CommandParameter {

    /**
     * @return the name of the parameter
     */
    String getName();

    /**
     * @return the index of this parameter
     */
    int getPosition();

    /**
     * Sets the position of this parameter in a syntax
     * DO NOT USE THIS FOR ANY REASON unless it's necessary to do so
     *
     * @param position the position to set
     */
    @ApiStatus.Internal
    void setPosition(int position);

    /**
     * @return the value type of this parameter
     */
    Class<?> getType();

    /**
     * Get GenericType
     *
     * @return the full type of parameter if it has generic types
     */
    Type getGenericType();

    /**
     * Get the description of a parameter
     *
     * @return the description of a parameter
     */
    Description getDescription();

    /**
     * @return the default value if it's input is not present
     * in case of the parameter being optional
     */
    <T> OptionalValueSupplier<T> getDefaultValueSupplier();

    /**
     * @return whether this is an optional argument
     */
    boolean isOptional();


    /**
     * @return checks whether this parameter is a flag
     */
    boolean isFlag();

    /**
     * Casts the parameter to a flag parameter
     *
     * @return the parameter as a flag
     */
    FlagParameter asFlagParameter();

    /**
     * @return checks whether this parameter
     * consumes all the args input after it.
     */
    boolean isGreedy();

    /**
     * @return checks whether this usage param is a command name
     */
    default boolean isCommand() {
        return this instanceof Command;
    }

    /**
     * Casts the parameter to a subcommand/command
     *
     * @return the parameter as a command
     */
    Command<?> asCommand();

    /**
     * @return Whether this usage parameter has been constructed
     * using the annotations through methods or not
     */
    default boolean isAnnotated() {
        return this instanceof AnnotatedParameter;
    }

    /**
     * Casts the parameter to a parameter with annotations
     *
     * @return the parameter as annotated one
     * @see AnnotatedParameter
     */
    default AnnotatedParameter asAnnotated() {
        return (AnnotatedParameter) this;
    }

    /**
     * Fetches the suggestion resolver linked to this
     * command parameter.
     *
     * @param <C> the command-sender type
     * @param <T> the type of value to be resolved
     * @return the {@link SuggestionResolver} for resolving suggestion
     */
    @Nullable
    <C, T> SuggestionResolver<C, T> getSuggestionResolver();


    /**
     * Formats the usage parameter*
     *
     * @return the formatted parameter
     */
    <C> String format();


    static <C, T> CommandParameter of(
            String name,
            Class<T> type,
            boolean optional,
            boolean greedy,
            OptionalValueSupplier<T> valueSupplier,
            SuggestionResolver<C, T> suggestionResolver
    ) {
        return new NormalCommandParameter(name, type, optional, greedy, valueSupplier, suggestionResolver);
    }

    static <C, T> CommandParameter required(String name, Class<T> clazz,
                                            SuggestionResolver<C, T> suggestionResolver) {
        return new NormalCommandParameter(name, clazz, false, false, null, suggestionResolver);
    }


    static <C, T> CommandParameter optional(
            String name, Class<T> clazz,
            @Nullable OptionalValueSupplier<T> defaultValue,
            SuggestionResolver<C, T> suggestionResolver
    ) {
        return new NormalCommandParameter(name, clazz, true, false, defaultValue, suggestionResolver);
    }

    static <C> CommandParameter greedy(
            String name,
            boolean optional,
            @Nullable OptionalValueSupplier<String> defaultValue,
            SuggestionResolver<C, String> suggestionResolver
    ) {
        return new NormalCommandParameter(
                name, String.class, optional,
                true, defaultValue, suggestionResolver
        );
    }

    static <C> CommandParameter requiredText(String name, SuggestionResolver<C, String> suggestionResolver) {
        return required(name, String.class, suggestionResolver);
    }

    static <C> CommandParameter requiredInt(String name, SuggestionResolver<C, Integer> suggestionResolver, @Nullable NumericRange range) {
        return NumericParameterDecorator.decorate(required(name, Integer.class, suggestionResolver), range);
    }

    static <C> CommandParameter requiredLong(String name, SuggestionResolver<C, Long> suggestionResolver, @Nullable NumericRange range) {
        return NumericParameterDecorator.decorate(required(name, Long.class, suggestionResolver), range);
    }

    static <C> CommandParameter requiredDouble(String name, SuggestionResolver<C, Double> suggestionResolver, @Nullable NumericRange range) {
        return NumericParameterDecorator.decorate(required(name, Double.class, suggestionResolver), range);
    }

    static FlagParameter flag(
            String flagName,
            List<String> aliases,
            Class<?> inputType,
            OptionalValueSupplier<?> supplier
    ) {
        return new FlagCommandParameter(flagName, aliases, inputType, supplier);
    }

    static FlagParameter switchParam(String flagName, List<String> aliases) {
        return new FlagCommandParameter(CommandFlag.createSwitch(flagName, aliases), new BooleanValueSupplier());
    }


    static <T> CommandParameter of(
            String name,
            Class<T> type,
            boolean optional,
            boolean greedy,
            OptionalValueSupplier<T> valueSupplier
    ) {
        return of(name, type, optional, greedy, valueSupplier, null);
    }

    static <T> CommandParameter required(String name, Class<T> clazz) {
        return required(name, clazz, null);
    }

    static <T> CommandParameter optional(
            String name, Class<T> clazz,
            @Nullable OptionalValueSupplier<T> defaultValue
    ) {
        return optional(name, clazz, defaultValue, null);
    }

    static CommandParameter greedy(
            String name,
            boolean optional,
            @Nullable OptionalValueSupplier<String> defaultValue
    ) {
        return greedy(name, optional, defaultValue, null);
    }

    static CommandParameter requiredText(String name) {
        return requiredText(name, null);
    }

    //TODO more overloading for each parameter
    static CommandParameter requiredInt(String name) {
        return requiredInt(name, null, null);
    }

    static CommandParameter requiredLong(String name) {
        return requiredLong(name, null, null);
    }

    static CommandParameter requiredDouble(String name) {
        return requiredDouble(name, null, null);
    }

    default NumericParameterDecorator asNumeric() {
        return (NumericParameterDecorator) this;
    }

    default boolean isNumeric() {
        return this instanceof NumericParameterDecorator;
    }


}
