package dev.velix.imperat.command.parameters;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.Description;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.supplier.OptionalValueSupplier;
import dev.velix.imperat.util.TypeUtility;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@ApiStatus.Internal
public abstract class InputParameter implements CommandParameter {

    protected final String name;
    protected final TypeWrap<?> typeWrap;
    protected final boolean optional, flag, greedy;
    protected final OptionalValueSupplier<?> optionalValueSupplier;
    protected final SuggestionResolver<?, ?> suggestionResolver;
    protected String permission;
    protected Description description;
    protected int index;


    protected InputParameter(String name, TypeWrap<?> typeWrap,
                             @Nullable String permission,
                             Description description,
                             boolean optional, boolean flag, boolean greedy,
                             OptionalValueSupplier<?> optionalValueSupplier, SuggestionResolver<?, ?> suggestionResolver) {
        this.name = name;
        this.typeWrap = typeWrap;
        this.permission = permission;
        this.description = description;
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

    @Override
    public TypeWrap<?> getTypeWrap() {
        return typeWrap;
    }

    /**
     * The permission for this parameter
     *
     * @return the parameter permission
     */
    @Override
    public @Nullable String getPermission() {
        return permission;
    }

    @Override
    public void setPermission(String permission) {
        this.permission = permission;
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
        if (this.typeWrap.getType() != String.class && greedy) {
            throw new IllegalStateException(
                    String.format("Usage parameter '%s' cannot be greedy while having value-type '%s'", name, getType().getTypeName())
            );
        }
        return greedy;
    }

    @Override
    public <S extends Source> Command<S> asCommand() {
        throw new UnsupportedOperationException("Non-Command Parameter cannot be converted into a command parameter");
    }


    /**
     * Fetches the suggestion resolver linked to this
     * command parameter.
     *
     * @return the {@link SuggestionResolver} for a resolving suggestion
     */
    @Override
    @SuppressWarnings("unchecked")
    public @Nullable <S extends Source, T> SuggestionResolver<S, T> getSuggestionResolver() {
        return (SuggestionResolver<S, T>) suggestionResolver;
    }

    @Override
    public Description getDescription() {
        return description;
    }

    @Override
    public void setDescription(Description description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InputParameter that = (InputParameter) o;
        return Objects.equals(name, that.name)
                && TypeUtility.matches(typeWrap.getType(), that.typeWrap.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, typeWrap);
    }

    @Override
    public String toString() {
        return format();
    }
}
