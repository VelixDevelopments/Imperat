package dev.velix.imperat.command;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.processors.CommandPostProcessor;
import dev.velix.imperat.command.processors.CommandPreProcessor;
import dev.velix.imperat.command.suggestions.AutoCompleter;
import dev.velix.imperat.command.tree.CommandDispatch;
import dev.velix.imperat.command.tree.CommandTree;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.SuggestionContext;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.supplier.OptionalValueSupplier;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

//TODO create processed String instance to wrap around a string with

/**
 * Represents a wrapper for the actual command's data
 *
 * @param <S> the command sender type
 */
@ApiStatus.AvailableSince("1.0.0")
public interface Command<S extends Source> extends CommandParameter<S> {

    static <S extends Source> Command.Builder<S> create(String name) {
        return create(null, name);
    }

    static <S extends Source> Command.Builder<S> create(@Nullable Command<S> parent, @NotNull String name) {
        return create(parent, 0, name);
    }

    static <S extends Source> Command.Builder<S> create(
            @Nullable Command<S> parent,
            int position,
            @NotNull String name
    ) {
        return new Builder<>(parent, position, name);
    }

    /**
     * @return The name of the command
     */
    String name();

    /**
     * @return The description of a command
     */
    @NotNull
    Description description();

    /**
     * @return The aliases for this commands
     */
    @UnmodifiableView
    List<String> aliases();

    /**
     * @return The tree for the command
     */
    CommandTree<S> tree();

    /**
     * Sets the aliases of a command
     *
     * @param aliases the aliases for te command to set
     */
    void addAliases(List<String> aliases);

    default void addAliases(String... aliases) {
        addAliases(List.of(aliases));
    }

    /**
     * Sets the position of this command in a syntax
     * DO NOT USE THIS FOR ANY REASON unless it's necessary to do so
     *
     * @param position the position to set
     */
    @ApiStatus.Internal
    default void position(int position) {
        throw new UnsupportedOperationException("You can't modify the position of a command");
    }

    /**
     * @return the default value if it's input is not present
     * in case of the parameter being optional
     */
    @Override
    @SuppressWarnings("unchecked")
    default <T> @NotNull OptionalValueSupplier<T> getDefaultValueSupplier() {
        return (OptionalValueSupplier<T>) OptionalValueSupplier.of(name());
    }

    /**
     * @param name the name used
     * @return Whether this command has this name/alias
     */
    default boolean hasName(String name) {
        return this.name().equalsIgnoreCase(name) || this.aliases().contains(name.toLowerCase());
    }

    /**
     * Traverses the {@link CommandTree} linked to
     * this command object, searching for the most suitable usage that
     * best suites the context input by the user
     *
     * @param context the context of the execution
     */
    @NotNull CommandDispatch<S> contextMatch(Context<S> context);

    /**
     * Traverses and searches in {@link CommandTree} , visiting every node
     *
     * @param dispatcher the dispatcher
     * @param context    the context
     * @return the auto-completed results
     */
    Collection<String> tabComplete(Imperat<S> dispatcher, SuggestionContext<S> context);

    /**
     * Debugs or visualizes all tree nodes
     * from {@link CommandTree}.
     * If the command is not a root command,
     * nothing will be visualized.
     */
    void visualizeTree();

    /**
     * Sets a pre-processor for the command
     *
     * @param preProcessor the pre-processor for the command
     */
    void setPreProcessor(@NotNull CommandPreProcessor<S> preProcessor);

    /**
     * Executes the pre-processing instructions in {@link CommandPreProcessor}
     *
     * @param api     the api
     * @param context the context
     * @param usage   the usage detected being used
     */
    void preProcess(@NotNull Imperat<S> api, @NotNull Context<S> context, @NotNull CommandUsage<S> usage) throws ImperatException;

    /**
     * Sets a post-processor for the command
     *
     * @param postProcessor the post-processor for the command
     */
    void setPostProcessor(@NotNull CommandPostProcessor<S> postProcessor);

    /**
     * Executes the post-processing instructions in {@link CommandPostProcessor}
     *
     * @param api     the api
     * @param context the context
     * @param usage   the usage detected being used
     */
    void postProcess(@NotNull Imperat<S> api, @NotNull ResolvedContext<S> context, @NotNull CommandUsage<S> usage) throws ImperatException;

    /**
     * @return the default usage of the command
     * without any args
     */
    @NotNull
    CommandUsage<S> getDefaultUsage();

    /**
     * @param execution sets what happens when there are no parameters
     */
    void setDefaultUsageExecution(CommandExecution<S> execution);

    /**
     * Adds a usage to the command
     *
     * @param usage the usage {@link CommandUsage} of the command
     */
    void addUsage(CommandUsage<S> usage);

    default void addUsage(CommandUsage.Builder<S> builder, boolean help) {
        addUsage(builder.build(this, help));
    }

    default void addUsage(CommandUsage.Builder<S> builder) {
        addUsage(builder, false);
    }

    /**
     * Fetches the usage with specific sequence of parameters
     *
     * @param parameters the parameters
     * @return the usage from its sequence of parameters, null if no usage has such a sequence of parameters
     */
    @Nullable CommandUsage<S> getUsage(List<CommandParameter<S>> parameters);

    /**
     * @return All {@link CommandUsage} that were registered
     * to this command by the user
     */
    Collection<? extends CommandUsage<S>> usages();

    /**
     * @param predicate the criteria
     * @return a list of usages that match a certain criteria
     */
    Collection<? extends CommandUsage<S>> findUsages(Predicate<CommandUsage<S>> predicate);

    /**
     * @return the usage that doesn't include any subcommands, only
     * parameters
     */
    @NotNull
    CommandUsage<S> mainUsage();

    /**
     * @return Returns {@link AutoCompleter}
     * that handles all auto-completes for this command
     */
    AutoCompleter<S> autoCompleter();

    /**
     * Injects a created-subcommand directly into the parent's command usages.
     *
     * @param command        the subcommand to inject
     * @param attachDirectly whether the sub command's usage will be attached to
     *                       the main/default usage of the command directly or not
     */
    void addSubCommand(Command<S> command, boolean attachDirectly);

    /**
     * Creates and adds a new sub-command (if it doesn't exist) then add
     * the {@link CommandUsage} to the sub-command
     *
     * @param subCommand     the sub-command's unique name
     * @param aliases        of the subcommand
     * @param usage          the usage
     * @param attachDirectly whether the sub command's usage will be attached to
     *                       the main/default usage of the command directly or not
     *                       <p>
     *                       if you have the command's default usage '/group' for example
     *                       and then you add the usage with attachDirectly being true, the usage
     *                       added will be in the form of "/command your subcommand param1 param2"
     *                       However, if you set attachDirectly to false, this will merge all the command's usages
     *                       automatically with the subcommand's usage, so if your command has a usage of '/command param1'
     *                       then the final usage will be : "/command param1 your subcommand param2, param3"
     *                       </p>
     */
    void addSubCommandUsage(String subCommand,
                            List<String> aliases,
                            CommandUsage.Builder<S> usage,
                            boolean attachDirectly);

    default void addSubCommandUsage(String subCommand,
                                    List<String> aliases,
                                    CommandUsage.Builder<S> usage) {
        addSubCommandUsage(subCommand, aliases, usage, false);
    }

    default void addSubCommandUsage(String subCommand,
                                    CommandUsage.Builder<S> usage,
                                    boolean attachDirectly) {
        addSubCommandUsage(subCommand, Collections.emptyList(), usage, attachDirectly);
    }

    /**
     * Creates and adds a new sub-command (if it doesn't exist) then add
     * the {@link CommandUsage} to the sub-command
     *
     * @param subCommand the sub-command's unique name
     * @param usage      the usage
     */
    default void addSubCommandUsage(String subCommand, CommandUsage.Builder<S> usage) {
        addSubCommandUsage(subCommand, usage, false);
    }

    /**
     * @param name the name of the wanted sub-command
     * @return the sub-command of specific name
     */
    @Nullable
    Command<S> getSubCommand(String name);

    /**
     * @return the subcommands of this command
     */
    @NotNull
    Collection<? extends Command<S>> getSubCommands();

    default @Nullable CommandUsage<S> getUsage(Predicate<CommandUsage<S>> usagePredicate) {
        for (var usage : usages()) {
            if (usagePredicate.test(usage)) {
                return usage;
            }
        }
        return null;
    }

    default boolean hasParent() {
        return parent() != null;
    }

    default boolean isSubCommand() {
        return hasParent();
    }

    /**
     * @return the value type of this parameter
     */
    @Override
    default TypeWrap<?> wrappedType() {
        return TypeWrap.of(Command.class);
    }

    /**
     * @return whether this is an optional argument
     */
    @Override
    default boolean isOptional() {
        return false;
    }

    /**
     * @return checks whether this parameter
     * consumes all the args input after it.
     */
    @Override
    default boolean isGreedy() {
        return false;
    }

    /**
     * @return checks whether this parameter is a flag
     */
    @Override
    default boolean isFlag() {
        return false;
    }

    @Override
    @SuppressWarnings("all")
    default Command<S> asCommand() {
        return (Command<S>) this;
    }

    /**
     * Formats the usage parameter
     *
     * @return the formatted parameter
     */
    @Override
    default String format() {
        return name();
    }

    /**
     * whether to ignore permission checks on the auto-completion of command and
     * sub commands or not
     *
     * @return whether to ignore permission checks on the auto-completion of command and
     * sub commands or not
     */
    boolean isIgnoringACPerms();

    /**
     * if true, it will ignore permission checks
     * on the auto-completion of command and sub commands
     * <p>
     * otherwise, it will perform permission checks and
     * only tab-completes the usages/subcommands that you have permission for
     *
     * @param ignore true if you want to ignore the permission checks on tab completion of args
     */
    void ignoreACPermissions(boolean ignore);

    /**
     * Adds help as a sub-command to the command chain
     *
     * @param dispatcher    the api
     * @param params        the parameters of the help command
     * @param helpExecution the help execution
     */
    void addHelpCommand(Imperat<S> dispatcher,
                        List<CommandParameter<S>> params,
                        CommandExecution<S> helpExecution);


    class Builder<S extends Source> {

        private final Command<S> cmd;

        Builder(@Nullable Command<S> parent, int position, String name) {
            this.cmd = new CommandImpl<>(parent, position, name);
        }

        public Builder<S> ignoreACPermissions(boolean ignore) {
            this.cmd.ignoreACPermissions(ignore);
            return this;
        }

        public Builder<S> aliases(String... aliases) {
            this.cmd.addAliases(aliases);
            return this;
        }

        public Builder<S> aliases(List<String> aliases) {
            this.cmd.addAliases(aliases);
            return this;
        }

        public Builder<S> description(String description) {
            this.cmd.describe(description);
            return this;
        }

        public Builder<S> description(Description description) {
            return description(description.toString());
        }

        public Builder<S> permission(String permission) {
            this.cmd.permission(permission);
            return this;
        }

        public Builder<S> defaultExecution(CommandExecution<S> defaultExec) {
            cmd.setDefaultUsageExecution(defaultExec);
            return this;
        }

        public Builder<S> usage(CommandUsage.Builder<S> usage) {
            cmd.addUsage(usage);
            return this;
        }

        public Builder<S> subCommand(Command<S> subCommand, boolean attachDirectly) {
            cmd.addSubCommand(subCommand, attachDirectly);
            return this;
        }

        public Builder<S> subCommand(Command<S> subCommand) {
            return subCommand(subCommand, false);
        }

        public Builder<S> subCommand(String name, CommandUsage.Builder<S> mainUsage, boolean attachDirectly) {
            return subCommand(
                    Command.<S>create(name)
                            .usage(mainUsage)
                            .build(),
                    attachDirectly
            );
        }

        public Builder<S> subCommand(String name, CommandUsage.Builder<S> mainUsage) {
            return subCommand(name, mainUsage, false);
        }

        public Builder<S> preProcessor(CommandPreProcessor<S> preProcessor) {
            cmd.setPreProcessor(preProcessor);
            return this;
        }

        public Builder<S> postProcessor(CommandPostProcessor<S> postProcessor) {
            cmd.setPostProcessor(postProcessor);
            return this;
        }

        public Command<S> build() {
            return cmd;
        }

    }

}
