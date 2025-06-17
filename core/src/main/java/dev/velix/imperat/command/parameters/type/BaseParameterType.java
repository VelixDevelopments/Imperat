package dev.velix.imperat.command.parameters.type;

import dev.velix.imperat.ImperatConfig;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.tree.CommandTree;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.util.TypeCapturer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for defining parameter types in a command processing framework.
 * This class handles the basic functionality for managing type information and
 * suggestions for parameters.
 *
 * @param <S> The type of the source from which the command originates.
 * @param <T> The type of the parameter being handled.
 */
public abstract class BaseParameterType<S extends Source, T> 
    extends TypeCapturer implements ParameterType<S, T> {

    /**
     * Encapsulates type information for the parameter being handled.
     * This instance is used to collect and manage type-related information
     * and operations specific to the parameter type.
     */
    protected final Type type;
    /**
     * A list storing suggestions for parameter types
     * in a command processing framework.
     */
    protected final List<String> suggestions = new ArrayList<>();

    /**
     * Constructs a new BaseParameterType with the given TypeWrap.
     *
     * @param type The wrapping object that holds information about the type of the parameter.
     */
    public BaseParameterType(final Type type) {
        this.type = type;
    }

    /**
     * Constructs a new BaseParameterType with an automated {@link TypeWrap}
     */
    public BaseParameterType() {
        this.type = this.extractType(1);
    }

    /**
     * Retrieves the Type associated with this BaseParameterType.
     *
     * @return the Type associated with this parameter type.
     */
    @Override
    public Type type() {
        return type;
    }

    /**
     * Returns the suggestion resolver associated with this parameter type.
     *
     * @return the suggestion resolver for generating suggestions based on the parameter type.
     */
    @Override
    public SuggestionResolver<S> getSuggestionResolver() {
        return suggestions.isEmpty() ?  null : SuggestionResolver.plain(suggestions);
    }

    /**
     * Determines whether the provided input matches the expected format or criteria
     * for a given command parameter. this is used during {@link CommandTree#contextMatch(ArgumentQueue, ImperatConfig)}
     *
     * @param input     The input string to be matched against the parameter criteria.
     * @param parameter The command parameter that provides context for the input handling.
     * @return true if the input matches the expected criteria; false otherwise.
     */
    @Override
    public boolean matchesInput(String input, CommandParameter<S> parameter) {
        return true;
    }

    /**
     * Adds the provided suggestions to the current list of suggestions for this parameter type.
     *
     * @param suggestions The array of suggestions to be added.
     * @return The current instance of the parameter type with the added suggestions.
     */
    @Override
    public @NotNull ParameterType<S, T> withSuggestions(String... suggestions) {
        this.suggestions.addAll(List.of(suggestions));
        return this;
    }

    @Override
    @SupressWarnings("all")
    protected Type extractType(int index) {
        Type genericSuperclass = ((BaseParameterType)this).getClass().getGenericSuperclass();

        if (genericSuperclass instanceof ParameterizedType parameterized) {
            Type[] args = parameterized.getActualTypeArguments();
            if (index < 0 || index >= args.length) {
                throw new IndexOutOfBoundsException("No type argument at index " + index);
            }
            return args[index];
        } 

        throw new IllegalStateException("Superclass is not parameterized: " + genericSuperclass);
    }

}
