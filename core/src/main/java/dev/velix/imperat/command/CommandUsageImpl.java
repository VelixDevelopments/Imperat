package dev.velix.imperat.command;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.cooldown.CooldownHandler;
import dev.velix.imperat.command.cooldown.DefaultCooldownHandler;
import dev.velix.imperat.command.cooldown.UsageCooldown;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.CommandFlag;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static dev.velix.imperat.util.Patterns.DOUBLE_FLAG;
import static dev.velix.imperat.util.Patterns.SINGLE_FLAG;

@ApiStatus.Internal
final class CommandUsageImpl<S extends Source> implements CommandUsage<S> {

    private final List<CommandParameter<S>> parameters = new ArrayList<>();
    private final List<CommandParameter<S>> parametersWithoutFlags = new ArrayList<>();
    private final @NotNull CommandExecution<S> execution;
    private final boolean help;
    private String permission = null;
    private Description description = Description.of("N/A");
    private @NotNull CooldownHandler<S> cooldownHandler;
    private @Nullable UsageCooldown cooldown = null;
    private CommandCoordinator<S> commandCoordinator;

    CommandUsageImpl(@NotNull CommandExecution<S> execution) {
        this(execution, false);
    }

    CommandUsageImpl(@NotNull CommandExecution<S> execution, boolean help) {
        this.execution = execution;
        this.cooldownHandler = new DefaultCooldownHandler<>(this);
        this.commandCoordinator = CommandCoordinator.sync();
        this.help = help;
    }

    /**
     * @return the permission for this usage
     */
    @Override
    public @Nullable String permission() {
        return permission;
    }

    /**
     * The permission for this usage
     *
     * @param permission permission to set
     */
    @Override
    public void permission(String permission) {
        this.permission = permission;
    }

    /**
     * @return the description for the
     * command usage
     */
    @Override
    public Description description() {
        return description;
    }

    @Override
    public void describe(Description description) {
        this.description = description;
    }

    /**
     * Checks whether the raw input is a flag
     * registered by this usage
     *
     * @param input the raw input
     * @return Whether the input is a flag and is registered in the usage
     */
    @Override
    public boolean hasFlag(String input) {
        return getFlagFromRaw(input) != null;
    }

    /**
     * Fetches the flag from the input
     *
     * @param rawInput the input
     * @return the flag from the raw input, null if it cannot be a flag
     */
    @Override
    public @Nullable CommandFlag getFlagFromRaw(String rawInput) {
        boolean isSingle = SINGLE_FLAG.matcher(rawInput).matches();
        boolean isDouble = DOUBLE_FLAG.matcher(rawInput).matches();

        if (!isSingle && !isDouble) {
            return null;
        }

        String inputFlagAlias = rawInput.substring(isSingle ? 1 : 2);

        for (var param : parameters) {
            if (!param.isFlag()) continue;
            CommandFlag flag = param.asFlagParameter().flagData();
            if (flag.acceptsInput(inputFlagAlias)) {
                return flag;
            }
        }

        return null;
    }


    /**
     * Adds parameters to the usage
     *
     * @param params the parameters to add
     */
    @SafeVarargs
    @Override
    public final void addParameters(CommandParameter<S>... params) {
        Collections.addAll(parameters, params);
        for (var param : params) {
            if (param.isFlag()) continue;
            parametersWithoutFlags.add(param);
        }
    }

    /**
     * Adds parameters to the usage
     *
     * @param params the parameters to add
     */
    @Override
    public void addParameters(List<CommandParameter<S>> params) {
        for (var p : params) {
            parameters.add(p);
            if (p.isFlag()) continue;
            parametersWithoutFlags.add(p);
        }
    }

    /**
     * @return the parameters for this usage
     * @see CommandParameter
     */
    @Override
    public List<CommandParameter<S>> getParameters() {
        return parameters;
    }

    @Override
    public List<CommandParameter<S>> getParametersWithoutFlags() {
        return parametersWithoutFlags;
    }

    @Override
    public @Nullable CommandParameter<S> getParameter(int index) {
        if (index < 0 || index >= parameters.size()) return null;
        return parameters.get(index);
    }

    /**
     * @return the execution for this command
     */
    @Override
    public @NotNull CommandExecution<S> getExecution() {
        return execution;
    }

    /**
     * @param clazz the type of the parameter to check upon
     * @return Whether the usage has a specific type of parameter
     */
    @Override
    public boolean hasParamType(Class<?> clazz) {
        return getParameters()
            .stream()
            .anyMatch((param) -> param.type().equals(clazz));
    }

    /**
     * @return Gets the minimal possible number
     * of parameters that are acceptable to initiate this
     * usage of a command.
     */
    @Override
    public int getMinLength() {
        return (int) getParameters().stream()
            .filter((param) -> !param.isFlag())
            .filter((param) -> !param.isOptional())
            .count();
    }

    /**
     * @return Gets the maximum possible number
     * of parameters that are acceptable to initiate this
     * usage of a command.
     */
    @Override
    public int getMaxLength() {
        return getParameters().size();
    }

    /**
     * Searches for a parameter with specific type
     *
     * @param parameterPredicate the parameter condition
     * @return whether this usage has atLeast on {@link CommandParameter} with specific condition
     * or not
     */
    @Override
    public boolean hasParameters(Predicate<CommandParameter<S>> parameterPredicate) {
        for (CommandParameter<S> parameter : getParameters())
            if (parameterPredicate.test(parameter))
                return true;

        return false;
    }

    /**
     * @param parameterPredicate the condition
     * @return the parameter to get using a condition
     */
    @Override
    public @Nullable CommandParameter<S> getParameter(Predicate<CommandParameter<S>> parameterPredicate) {
        for (CommandParameter<S> parameter : getParameters()) {
            if (parameterPredicate.test(parameter))
                return parameter;
        }
        return null;
    }

    /**
     * @return The cooldown for this usage {@link UsageCooldown}
     * returns null if no cooldown has been set
     */
    @Override
    public @Nullable UsageCooldown getCooldown() {
        return cooldown;
    }

    /**
     * Sets the command usage's cooldown {@link UsageCooldown}
     *
     * @param usageCooldown the cool down for this usage
     */
    @Override
    public void setCooldown(@Nullable UsageCooldown usageCooldown) {
        this.cooldown = usageCooldown;
    }

    /**
     * @return the cool down handler {@link CooldownHandler}
     */
    @Override
    public @NotNull CooldownHandler<S> getCooldownHandler() {
        return cooldownHandler;
    }

    /**
     * Sets the cooldown handler {@link CooldownHandler}
     *
     * @param cooldownHandler the cool down handler to set
     */
    @Override
    public void setCooldownHandler(@NotNull CooldownHandler<S> cooldownHandler) {
        this.cooldownHandler = cooldownHandler;
    }

    /**
     * @return the coordinator for execution of the command
     */
    @Override
    public CommandCoordinator<S> getCoordinator() {
        return commandCoordinator;
    }

    /**
     * Sets the command coordinator
     *
     * @param commandCoordinator the coordinator to set
     */
    @Override
    public void setCoordinator(CommandCoordinator<S> commandCoordinator) {
        this.commandCoordinator = commandCoordinator;
    }

    /**
     * Executes the usage's actions
     * using the supplied {@link CommandCoordinator}
     *
     * @param imperat the api
     * @param source  the command source/sender
     * @param context the context of the command
     */
    @Override
    public void execute(Imperat<S> imperat, S source, ExecutionContext<S> context) {
        commandCoordinator.coordinate(imperat, source, context, this.execution);
    }

    @Override
    public boolean isHelp() {
        return help;
    }

    @Override
    public boolean hasParameters(List<CommandParameter<S>> parameters) {
        return this.parameters.equals(parameters);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandUsageImpl<?> that = (CommandUsageImpl<?>) o;
        if (this.size() != that.size()) return false;
        for (int i = 0; i < this.size(); i++) {
            var thisP = this.getParameter(i);
            var thatP = that.getParameter(i);
            assert thisP != null;
            if (!thisP.similarTo(thatP)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameters);
    }
}
