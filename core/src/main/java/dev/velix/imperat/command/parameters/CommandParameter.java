package dev.velix.imperat.command.parameters;

import dev.velix.imperat.annotations.parameters.AnnotatedParameter;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.Description;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.supplier.OptionalValueSupplier;
import dev.velix.imperat.util.Preconditions;
import dev.velix.imperat.util.TypeUtility;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import java.lang.reflect.Type;

/**
 * Represents the command parameter required
 * by the usage of the command itself
 */
@ApiStatus.AvailableSince("1.0.0")
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
     * @return the {@link SuggestionResolver} for a resolving suggestion
     */
    @Nullable
    <C, T> SuggestionResolver<C, T> getSuggestionResolver();


    /**
     * Formats the usage parameter*
     *
     * @return the formatted parameter
     */
    String format();
    
    static <C, T> CommandParameter of(
            String name,
            Class<T> type,
            Description description,
            boolean optional,
            boolean greedy,
            OptionalValueSupplier<T> valueSupplier,
            SuggestionResolver<C, T> suggestionResolver
    ) {
        Preconditions.notNull(name, "name cannot be null !");
        Preconditions.notNull(type, "type cannot be null ");
        Preconditions.checkArgument(!TypeUtility.matches(type, Object.class), "Type cannot be `Object`");
        
        return new NormalCommandParameter(
                name, type, description, optional,
                greedy, valueSupplier, suggestionResolver
        );
    }
    
    static <C, T> ParameterBuilder<C, T> required(String name, Class<T> type) {
        return new ParameterBuilder<>(name, type, false);
    }
    
    static <C> ParameterBuilder<C, Integer> requiredInt(String name) {
        return required(name, Integer.class);
    }
    
    static <C> ParameterBuilder<C, Long> requiredLong(String name) {
        return required(name, Long.class);
    }
    
    static <C> ParameterBuilder<C, Double> requiredDouble(String name) {
        return required(name, Double.class);
    }
    
    static <C> ParameterBuilder<C, Float> requiredFloat(String name) {
        return required(name, Float.class);
    }
    
    static <C> ParameterBuilder<C, Boolean> requiredBoolean(String name) {
        return required(name, Boolean.class);
    }
    
    static <C> ParameterBuilder<C, String> requiredText(String name) {
        return required(name, String.class);
    }
    
    static <C> ParameterBuilder<C, String> requiredGreedy(String name) {
        return new ParameterBuilder<>(name, String.class, false, true);
    }
    
    static <C, T> ParameterBuilder<C, T> optional(String name, Class<T> type) {
        return new ParameterBuilder<>(name, type, true);
    }
    
    static <C> ParameterBuilder<C, Integer> optionalInt(String name) {
        return optional(name, Integer.class);
    }
    
    static <C> ParameterBuilder<C, Long> optionalLong(String name) {
        return optional(name, Long.class);
    }
    
    static <C> ParameterBuilder<C, Double> optionalDouble(String name) {
        return optional(name, Double.class);
    }
    
    static <C> ParameterBuilder<C, Float> optionalFloat(String name) {
        return optional(name, Float.class);
    }
    
    static <C> ParameterBuilder<C, Boolean> optionalBoolean(String name) {
        return optional(name, Boolean.class);
    }
    
    static <C> ParameterBuilder<C, String> optionalText(String name) {
        return optional(name, String.class);
    }

    
    static <C> ParameterBuilder<C, String> optionalGreedy(String name) {
        return new ParameterBuilder<>(name, String.class, true, true);
    }
    
    static <C, T> FlagBuilder<C, T> flag(
            String name,
            Class<T> inputType
    ) {
        return FlagBuilder.ofFlag(name, inputType);
    }
    
    static <C> FlagBuilder<C, Boolean> flagSwitch(String name) {
        return FlagBuilder.ofSwitch(name);
    }
    
    default boolean isNumeric() {
        return TypeUtility.isNumericType(this.getType());
    }
    
    default NumericParameter asNumeric() {
        return (NumericParameter) this;
    }
    
}
