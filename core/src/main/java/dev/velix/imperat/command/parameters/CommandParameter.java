package dev.velix.imperat.command.parameters;

import dev.velix.imperat.annotations.parameters.AnnotatedParameter;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.Describable;
import dev.velix.imperat.command.Description;
import dev.velix.imperat.command.PermissionHolder;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.supplier.OptionalValueSupplier;
import dev.velix.imperat.util.Preconditions;
import dev.velix.imperat.util.TypeUtility;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

/**
 * Represents the command parameter required
 * by the usage of the command itself
 */
@ApiStatus.AvailableSince("1.0.0")
public interface CommandParameter extends PermissionHolder, Describable {
    
    static <S extends Source, T> CommandParameter of(
            String name,
            TypeWrap<T> type,
            @Nullable String permission,
            Description description,
            boolean optional,
            boolean greedy,
            OptionalValueSupplier<T> valueSupplier,
            SuggestionResolver<S, T> suggestionResolver
    ) {
        Preconditions.notNull(name, "name");
        Preconditions.notNull(type, "type");
        Preconditions.checkArgument(!TypeUtility.matches(type.getType(), Object.class), "Type cannot be `Object`");
        
        return new NormalCommandParameter<S>(
                name, type, permission, description, optional,
                greedy, valueSupplier, suggestionResolver
        );
    }
    
    static <S extends Source, T> ParameterBuilder<S, T> required(String name, TypeWrap<T> type) {
        return new ParameterBuilder<>(name, type, false);
    }
    
    static <S extends Source, T> ParameterBuilder<S, T> required(String name, Class<T> type) {
        return required(name, TypeWrap.of(type));
    }
    
    static <S extends Source> ParameterBuilder<S, Integer> requiredInt(String name) {
        return required(name, Integer.class);
    }
    
    static <S extends Source> ParameterBuilder<S, Long> requiredLong(String name) {
        return required(name, Long.class);
    }
    
    static <S extends Source> ParameterBuilder<S, Double> requiredDouble(String name) {
        return required(name, Double.class);
    }
    
    static <S extends Source> ParameterBuilder<S, Float> requiredFloat(String name) {
        return required(name, Float.class);
    }
    
    static <S extends Source> ParameterBuilder<S, Boolean> requiredBoolean(String name) {
        return required(name, Boolean.class);
    }
    
    static <S extends Source> ParameterBuilder<S, String> requiredText(String name) {
        return required(name, String.class);
    }
    
    static <S extends Source> ParameterBuilder<S, String> requiredGreedy(String name) {
        return new ParameterBuilder<>(name, String.class, false, true);
    }
    
    static <S extends Source, T> ParameterBuilder<S, T> optional(String name, TypeWrap<T> token) {
        return new ParameterBuilder<>(name, token, true);
    }
    
    static <S extends Source, T> ParameterBuilder<S, T> optional(String name, Class<T> type) {
        return optional(name, TypeWrap.of(type));
    }
    
    static <S extends Source> ParameterBuilder<S, Integer> optionalInt(String name) {
        return optional(name, Integer.class);
    }
    
    static <S extends Source> ParameterBuilder<S, Long> optionalLong(String name) {
        return optional(name, Long.class);
    }
    
    static <S extends Source> ParameterBuilder<S, Double> optionalDouble(String name) {
        return optional(name, Double.class);
    }
    
    static <S extends Source> ParameterBuilder<S, Float> optionalFloat(String name) {
        return optional(name, Float.class);
    }
    
    static <S extends Source> ParameterBuilder<S, Boolean> optionalBoolean(String name) {
        return optional(name, Boolean.class);
    }
    
    static <S extends Source> ParameterBuilder<S, String> optionalText(String name) {
        return optional(name, String.class);
    }
    
    static <S extends Source> ParameterBuilder<S, String> optionalGreedy(String name) {
        return new ParameterBuilder<>(name, String.class, true, true);
    }
    
    static <S extends Source, T> FlagBuilder<S, T> flag(
            String name,
            Class<T> inputType
    ) {
        return FlagBuilder.ofFlag(name, inputType);
    }
    
    static <S extends Source> FlagBuilder<S, Boolean> flagSwitch(String name) {
        return FlagBuilder.ofSwitch(name);
    }
    
    /**
     * @return the name of the parameter
     */
    String name();
    
    /**
     * @return the parent of this parameter
     */
    @Nullable Command<?> parent();
    
    /**
     * Sets parent command for a parameter
     *
     * @param parentCommand the parameter's owning command
     */
    void parent(Command<?> parentCommand);
    
    /**
     * @return the index of this parameter
     */
    int position();
    
    /**
     * Sets the position of this parameter in a syntax
     * DO NOT USE THIS FOR ANY REASON unless it's necessary to do so
     *
     * @param position the position to set
     */
    @ApiStatus.Internal
    void position(int position);
    
    /**
     * @return the value type-token of this parameter
     */
    TypeWrap<?> wrappedType();
    
    /**
     * @return the value type of this parameter
     */
    default Type type() {
        return wrappedType().getType();
    }

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
    <S extends Source> Command<S> asCommand();
    
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
     * @param <S> the command-sender type
     * @param <T> the type of value to be resolved
     * @return the {@link SuggestionResolver} for a resolving suggestion
     */
    @Nullable
    <S extends Source, T> SuggestionResolver<S, T> getSuggestionResolver();
    
    /**
     * Formats the usage parameter*
     *
     * @return the formatted parameter
     */
    String format();
    
    default boolean isNumeric() {
        return this instanceof NumericParameter;
    }
    
    default NumericParameter asNumeric() {
        return (NumericParameter) this;
    }
    
    /**
     * Checks if this parameter has same name and type to the other {@link CommandParameter}
     * unlike `CommandParameter#equals(Object)`,
     * if both parameters are only different in their parent {@link Command},
     * it would still return true
     *
     * @param parameter the parameter to compare to
     * @return Whether this parameter has same name and type to the other {@link CommandParameter} or not
     */
    boolean similarTo(CommandParameter parameter);
    
}
