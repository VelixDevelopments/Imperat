package dev.velix.imperat.command.parameters;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.Description;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.supplier.OptionalValueSupplier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Objects;

@ApiStatus.Internal
public abstract class InputParameter implements CommandParameter {

    protected final String name;
    protected int index;
    protected final Class<?> type;
    protected final boolean optional, flag, greedy;
    protected final OptionalValueSupplier<?> optionalValueSupplier;
    protected final SuggestionResolver<?, ?> suggestionResolver;
    protected final Description description;
    
    
    protected InputParameter(String name, Class<?> type,
                             Description description,
                             boolean optional, boolean flag, boolean greedy,
                             OptionalValueSupplier<?> optionalValueSupplier, SuggestionResolver<?, ?> suggestionResolver) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.optional = optional;
        this.flag = flag;
        this.greedy = greedy;
        this.optionalValueSupplier = optionalValueSupplier;
	      this.suggestionResolver = suggestionResolver;
    }
    
    protected InputParameter(String name, Class<?> type,
                             boolean optional, boolean flag, boolean greedy,
                             OptionalValueSupplier<?> optionalValueSupplier, SuggestionResolver<?, ?> suggestionResolver) {
        this.name = name;
        this.type = type;
        this.description = Description.EMPTY;
        this.optional = optional;
        this.flag = flag;
        this.greedy = greedy;
        this.optionalValueSupplier = optionalValueSupplier;
        this.suggestionResolver = suggestionResolver;
    }
    /**
     * @return the name of the parameter
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * @return the index of this parameter
     */
    @Override
    public int getPosition() {
        return index;
    }

    /**
     * Sets the position of this parameter in a syntax
     * DO NOT USE THIS FOR ANY REASON unless it's necessary to do so
     *
     * @param position the position to set
     */
    @Override
    public void setPosition(int position) {
        this.index = position;
    }

    /**
     * @return the value type of this parameter
     */
    @Override
    public Class<?> getType() {
        return type;
    }

    /**
     * @return the default value if it's input is not present
     * in case of the parameter being optional
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> OptionalValueSupplier<T> getDefaultValueSupplier() {
        return (OptionalValueSupplier<T>) optionalValueSupplier;
    }

    /**
     * @return whether this is an optional argument
     */
    @Override
    public boolean isOptional() {
        return optional;
    }

    /**
     * @return checks whether this parameter is a flag
     */
    @Override
    public boolean isFlag() {
        return flag;
    }

    /**
     * Casts the parameter to a flag parameter
     *
     * @return the parameter as a flag
     */
    @Override
    public FlagParameter asFlagParameter() {
        return (FlagParameter) this;
    }

    /**
     * @return checks whether this parameter
     * consumes all the args input after it.
     */
    @Override
    public boolean isGreedy() {
        if (this.type != String.class && greedy) {
            throw new IllegalStateException(
                    String.format("Usage parameter '%s' cannot be greedy while having value-type '%s'", name, type.getName())
            );
        }
        return greedy;
    }

    @Override
    public Command<?> asCommand() {
        throw new UnsupportedOperationException("Non-Command Parameter cannot be converted into a command parameter");
    }
    
    
    /**
     * Fetches the suggestion resolver linked to this
     * command parameter.
     *
     * @return the {@link SuggestionResolver} for a resolving suggestion
     */
    @Override @SuppressWarnings("unchecked")
    public @Nullable <C, T> SuggestionResolver<C, T> getSuggestionResolver() {
        return (SuggestionResolver<C, T>) suggestionResolver;
    }
    
    @Override
    public Type getGenericType() {
        return getType();
    }
    
    @Override
    public Description getDescription() {
        return description;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InputParameter that = (InputParameter) o;
        return Objects.equals(name, that.name)
                && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        return format(null);
    }
}
