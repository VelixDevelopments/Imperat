package dev.velix.imperat.command;

import dev.velix.imperat.CommandDebugger;
import dev.velix.imperat.CommandDispatcher;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.suggestions.AutoCompleter;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.help.CommandHelp;
import dev.velix.imperat.help.HelpExecution;
import dev.velix.imperat.help.PaginatedHelpTemplate;
import dev.velix.imperat.supplier.OptionalValueSupplier;
import dev.velix.imperat.util.ListUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
     * Sets the description of a command
     *
     * @param description the desc to set
     */
    void setDescription(String description);

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
     * @return All {@link CommandUsage} that were registered
     * to this command by the user
     */
    Collection<? extends CommandUsage<C>> getUsages();

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
     * @return the parent command of this sub-command
     */
    @Nullable
    Command<C> getParent();

    /**
     * The command to be added as a subcommand of this instance
     *
     * @param command the sub-command to be added
     */
    void addSubCommand(Command<C> command);


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
     *
     *                       </p>
     */
    //TODO create subcommand builder
    default void addSubCommandUsage(String subCommand,
                                    List<String> aliases,
                                    CommandUsage<C> usage,
                                    boolean attachDirectly) {

        Command<C> mapped = getSubCommand(subCommand.toLowerCase());
        if(mapped != null) {
            throw new UnsupportedOperationException("You can't add an already existing sub-command '" + subCommand + "' to command '" + this.getName() + "'");
        }
        
        int position;
        if (attachDirectly) {
            position = getPosition() + 1;
        } else {
            CommandUsage<C> main = getMainUsage();
            position = this.getPosition() + (main.getMinLength() == 0 ? 1 : main.getMinLength());
        }
        
        //creating subcommand to modify
        Command<C> subCmd = Command.createCommand(this, position, subCommand);
        subCmd.addAliases(aliases);
        subCmd.addUsage(usage);
        //subCmd.setPosition(position);
        
        //adding subcommand
        addSubCommand(subCmd);
        
        final CommandUsage<C> prime = attachDirectly ? getDefaultUsage() : getMainUsage();
        final CommandUsage<C> combo = prime.mergeWithCommand(subCmd, usage);
        //adding the merged command usage
        
        CommandDebugger.debug("Trying to add usage `%s`", CommandUsage.format(this, combo));
        this.addUsage(
                combo
        );
    }

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

    default CommandUsageLookup<C> lookup(CommandDispatcher<C> dispatcher) {
        return new CommandUsageLookup<>(dispatcher, this);
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
    default Class<?> getType() {
        return Command.class;
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
    default Command<C> asCommand() {
        return this;
    }

    /**
     * Formats the usage parameter
     *
     * @return the formatted parameter
     */
    @Override
    default  String format() {
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
     */
    @SuppressWarnings("unchecked")
    default void addHelpCommand(CommandDispatcher<C> dispatcher,
                                List<CommandParameter> params,
                                HelpExecution<C> helpExecution) {
        if (params.isEmpty() && dispatcher.getHelpTemplate() instanceof PaginatedHelpTemplate) {
            params.add(CommandParameter.optional("page", Integer.class,
                    OptionalValueSupplier.of(1)));
        }

        addSubCommandUsage(
                "help",
                CommandUsage.<C>builder()
                        .parameters(params)
                        .execute((sender, context) -> {
                            //CommandDebugger.debug("Executing help !");
                            Integer page = context.getArgument("page");
                            CommandHelp<C> help = dispatcher.createCommandHelp(this,
                                    (Context<C>) context, ((ResolvedContext<C>) context).getDetectedUsage());
                            helpExecution.help(sender, (Context<C>) context, help, page);
                        }).buildAsHelp(),
                false
        );
    }

    static <C> Command<C> createCommand(String name) {
        return createCommand(null, name);
    }

    static <C> Command<C> createCommand(@Nullable Command<C> parent, @NotNull String name) {
        return new CommandImpl<>(parent, name);
    }
    
    static <C> Command<C> createCommand(@Nullable Command<C> parent, int position, @NotNull String name) {
        return new CommandImpl<>(parent, position, name);
    }
    
}
