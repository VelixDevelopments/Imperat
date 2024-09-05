package dev.velix.imperat.command;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.processors.CommandPostProcessor;
import dev.velix.imperat.command.processors.CommandPreProcessor;
import dev.velix.imperat.command.suggestions.AutoCompleter;
import dev.velix.imperat.command.tree.CommandTree;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.exceptions.CommandException;
import dev.velix.imperat.help.HelpExecution;
import dev.velix.imperat.supplier.OptionalValueSupplier;
import dev.velix.imperat.command.tree.Traverse;
import dev.velix.imperat.util.ListUtils;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Represents a wrapper for the actual command's data
 * @param <C> the command sender type
 */
@ApiStatus.AvailableSince("1.0.0")
public interface Command<C> extends CommandParameter {

    /**
     * @return The name of the command
     */
    String getName();

    /**
     * @return The permission of the command
     */
    @Nullable
    String getPermission();

    /**
     * Sets the permission of a command
     *
     * @param permission the permission of a command
     */
    void setPermission(@Nullable String permission);

    /**
     * @return The description of a command
     */
    @NotNull
    Description getDescription();

    /**
     * @return The aliases for this commands
     */
    @UnmodifiableView
    List<String> getAliases();

    /**
     * Sets the aliases of a command
     *
     * @param aliases the aliases for te command to set
     */
    void addAliases(List<String> aliases);

    default void addAliases(String... aliases) {
        addAliases(Arrays.asList(aliases));
    }

    /**
     * Sets the position of this command in a syntax
     * DO NOT USE THIS FOR ANY REASON unless it's necessary to do so
     *
     * @param position the position to set
     */
    @ApiStatus.Internal
    default void setPosition(int position) {
        throw new UnsupportedOperationException("You can't modify the position of a command");
    }

    /**
     * @return the default value if it's input is not present
     * in case of the parameter being optional
     */
    @Override
    @SuppressWarnings("unchecked")
    default @Nullable <T> OptionalValueSupplier<T> getDefaultValueSupplier() {
        return (OptionalValueSupplier<T>) OptionalValueSupplier.of(getName());
    }

    /**
     * @param name the name used
     * @return Whether this command has this name/alias
     */
    default boolean hasName(String name) {
        return this.getName().equalsIgnoreCase(name) || ListUtils.contains(this.getAliases(), name);
    }
    
    /**
     * Traverses the {@link CommandTree} linked to
     * this command object, searching for the most suitable usage that
     * best suites the context input by the user
     *
     * @param context the context of the execution
     */
    @NotNull Traverse traverse(Context<C> context);
    
    /**
     * Sets a pre-processor for the command
     * @param preProcessor the pre-processor for the command
     */
    void setPreProcessor(@NotNull CommandPreProcessor<C> preProcessor);
    
    /**
     * Executes the pre-processing instructions in {@link CommandPreProcessor}
     * @param api the api
     * @param context the context
     * @param usage the usage detected being used
     */
    void preProcess(@NotNull Imperat<C> api, @NotNull Context<C> context, @NotNull CommandUsage<C> usage) throws CommandException;
    
    /**
     * Sets a post-processor for the command
     * @param postProcessor the post-processor for the command
     */
    void setPostProcessor(@NotNull CommandPostProcessor<C> postProcessor);
    
    /**
     * Executes the post-processing instructions in {@link CommandPostProcessor}
     * @param api the api
     * @param context the context
     * @param usage the usage detected being used
     */
    void postProcess(@NotNull Imperat<C> api, @NotNull ResolvedContext<C> context, @NotNull CommandUsage<C> usage) throws CommandException;

    /**
     * @return the default usage of the command
     * without any args
     */
    @NotNull
    CommandUsage<C> getDefaultUsage();

    /**
     * @param execution sets what happens when there are no parameters
     */
    void setDefaultUsageExecution(CommandExecution<C> execution);


    /**
     * Adds a usage to the command
     *
     * @param usage the usage {@link CommandUsage} of the command
     */
    void addUsage(CommandUsage<C> usage);
    
    /**
     * Fetches the usage with specific sequence of parameters
     * @param parameters the parameters
     * @return the usage from its sequence of parameters, null if no usage has such a sequence of parameters
     */
    @Nullable CommandUsage<C> getUsage(List<CommandParameter> parameters);

    /**
     * @return All {@link CommandUsage} that were registered
     * to this command by the user
     */
    Collection<? extends CommandUsage<C>> getUsages();
    
    /**
     * @param predicate the criteria
     * @return a list of usages that match a certain criteria
     */
    Collection<? extends CommandUsage<C>> findUsages(Predicate<CommandUsage<C>> predicate);
    
    /**
     * @return the usage that doesn't include any subcommands, only
     * parameters
     */
    @NotNull
    CommandUsage<C> getMainUsage();

    /**
     * @return Returns {@link AutoCompleter}
     * that handles all auto-completes for this command
     */
    AutoCompleter<C> getAutoCompleter();
    
    /**
     * sets the parent command
     * @param parent the parent to set.
     */
    void setParent(Command<C> parent);
    
    /**
     * @return the parent command of this sub-command
     */
    @Nullable
    Command<C> getParent();
    
    /**
     * Injects a created-subcommand directly into the parent's command usages.
     * @param command the subcommand to inject
     * @param attachDirectly  whether the sub command's usage will be attached to
     *                        the main/default usage of the command directly or not
     */
    void addSubCommand(Command<C> command, boolean attachDirectly);
    
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
                                    CommandUsage<C> usage,
                                    boolean attachDirectly);

    default void addSubCommandUsage(String subCommand,
                                    List<String> aliases,
                                    CommandUsage<C> usage) {
        addSubCommandUsage(subCommand, aliases, usage, false);
    }

    default void addSubCommandUsage(String subCommand,
                                    CommandUsage<C> usage,
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
    default void addSubCommandUsage(String subCommand, CommandUsage<C> usage) {
        addSubCommandUsage(subCommand, usage, false);
    }

    /**
     * @param name the name of the wanted sub-command
     * @return the sub-command of specific name
     */
    @Nullable
    Command<C> getSubCommand(String name);

    /**
     * @return the subcommands of this command
     */
    @NotNull
    Collection<? extends Command<C>> getSubCommands();
    
    default @Nullable CommandUsage<C> getUsage(Predicate<CommandUsage<C>> usagePredicate) {
        for(var usage : getUsages()) {
            if(usagePredicate.test(usage)) {
                return usage;
            }
        }
        return null;
    }

    default boolean hasParent() {
        return getParent() != null;
    }

    default boolean isSubCommand() {
        return hasParent();
    }

    /**
     * @return the value type of this parameter
     */
    @Override
    default TypeWrap<?> getTypeWrap() {
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

    @Override @SuppressWarnings("all")
    default <C> Command<C> asCommand() {
        return (Command<C>) this;
    }

    /**
     * Formats the usage parameter
     * @return the formatted parameter
     */
    @Override
    default String format() {
        return getName();
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
     * @param dispatcher the api
     * @param params the parameters of the help command
     * @param helpExecution the help execution
     */
    void addHelpCommand(Imperat<C> dispatcher,
                                List<CommandParameter> params,
                                HelpExecution<C> helpExecution);
    

    static <C> Command.Builder<C> create(String name) {
        return create(null, name);
    }

    static <C> Command.Builder<C> create(@Nullable Command<C> parent, @NotNull String name) {
        return create(parent, 0, name);
    }

    static <C> Command.Builder<C> create(
            @Nullable Command<C> parent,
            int position,
            @NotNull String name
    ) {
        return new Builder<>(parent, position, name);
    }
    
    class Builder<C> {
        
        private final Command<C> cmd;
        
        Builder(@Nullable Command<C> parent, int position, String name) {
            this.cmd = new CommandImpl<>(parent, position, name);
        }
        
        public Builder<C> ignoreACPermissions(boolean ignore) {
            this.cmd.ignoreACPermissions(ignore);
            return this;
        }
        
        public Builder<C> aliases(String... aliases) {
            this.cmd.addAliases(aliases);
            return this;
        }
        
        public Builder<C> aliases(List<String> aliases) {
            this.cmd.addAliases(aliases);
            return this;
        }
        
        public Builder<C> description(String description) {
            this.cmd.setDescription(description);
            return this;
        }
        
        public Builder<C> description(Description description) {
            return description(description.toString());
        }
        
        public Builder<C> permission(String permission) {
            this.cmd.setPermission(permission);
            return this;
        }
        
        public Builder<C> defaultExecution(CommandExecution<C> defaultExec) {
            cmd.setDefaultUsageExecution(defaultExec);
            return this;
        }
        
        public Builder<C> usage(CommandUsage<C> usage) {
            cmd.addUsage(usage);
            return this;
        }
        
        public Builder<C> subCommand(Command<C> subCommand, boolean attachDirectly) {
            cmd.addSubCommand(subCommand, attachDirectly);
            return this;
        }
        
        public Builder<C> subCommand(Command<C> subCommand) {
            return subCommand(subCommand, false);
        }
        
        public Builder<C> subCommand(String name, CommandUsage<C> mainUsage, boolean attachDirectly) {
            return subCommand(
                    Command.<C>create(name)
                            .usage(mainUsage)
                            .build(),
                    attachDirectly
            );
        }
        
        public Builder<C> subCommand(String name, CommandUsage<C> mainUsage) {
            return subCommand(name, mainUsage, false);
        }
        
        public Builder<C> preProcessor(CommandPreProcessor<C> preProcessor) {
            cmd.setPreProcessor(preProcessor);
            return this;
        }
        
        public Builder<C> postProcessor(CommandPostProcessor<C> postProcessor) {
            cmd.setPostProcessor(postProcessor);
            return this;
        }
        
        public Command<C> build() {
            return cmd;
        }
        
    }
	
}
