package dev.velix.imperat.command;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.FlagParameter;
import dev.velix.imperat.command.processors.CommandPostProcessor;
import dev.velix.imperat.command.processors.CommandPreProcessor;
import dev.velix.imperat.command.suggestions.AutoCompleter;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.exceptions.CommandException;
import dev.velix.imperat.help.CommandHelp;
import dev.velix.imperat.help.HelpExecution;
import dev.velix.imperat.help.PaginatedHelpTemplate;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.util.CommandDebugger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.lang.reflect.Type;
import java.util.*;

@ApiStatus.Internal
final class CommandImpl<C> implements Command<C> {

    private final String name;
    private String permission = null;
    private Description description = Description.EMPTY;

    private final int position;

    private boolean suppressACPermissionChecks = false;

    private final List<String> aliases = new ArrayList<>();

    private CommandUsage<C> defaultUsage;

    private @Nullable CommandPreProcessor<C> preProcessor;
    private @Nullable CommandPostProcessor<C> postProcessor;
    
    private final Command<C> parent;
    private final Map<String, Command<C>> children = new HashMap<>();
    private final Set<CommandUsage<C>> usages = new LinkedHashSet<>();
    
    private final AutoCompleter<C> autoCompleter;


    CommandImpl(String name) {
        this(null, name);
    }

    //sub-command constructor
    CommandImpl(@Nullable Command<C> parent, String name) {
        this(parent, 0, name);
    }

    CommandImpl(@Nullable Command<C> parent, int position, String name) {
        this.parent = parent;
        this.position = position;
        this.name = name;
        setDefaultUsageExecution((source, context) -> {
        });
        this.autoCompleter = AutoCompleter.createNative(this);
    }

    /**
     * @return the name of the command
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * @return The permission of the command
     */
    @Override
    public @Nullable String getPermission() {
        return permission;
    }

    /**
     * Sets the permission of a command
     *
     * @param permission the permission of a command
     */
    @Override
    public void setPermission(@Nullable String permission) {
        this.permission = permission;
    }

    /**
     * @return The description of a command
     */
    @Override
    public @NotNull Description getDescription() {
        return description;
    }

    /**
     * Sets the description of a command
     *
     * @param description the desc to set
     */
    @Override
    public void setDescription(String description) {
        this.description = Description.of(description);
    }


    /**
     * @return the index of this parameter
     */
    @Override
    public int getPosition() {
        return position;
    }

    /**
     * Sets the position of this command in a syntax
     * DO NOT USE THIS FOR ANY REASON unless it's necessary to do so
     *
     * @param position the position to set
     */
    @Override
    public void setPosition(int position) {
        throw new UnsupportedOperationException("You can't modify the position of a command");
    }
    
    /**
     * Sets a pre-processor for the command
     *
     * @param preProcessor the pre-processor for the command
     */
    @Override
    public void setPreProcessor(@NotNull CommandPreProcessor<C> preProcessor) {
        this.preProcessor = preProcessor;
    }
    
    /**
     * Executes the pre-processing instructions in {@link CommandPreProcessor}
     *
     * @param api     the api
     * @param context the context
     * @param usage   the usage detected being used
     */
    @Override
    public void preProcess(@NotNull Imperat<C> api, @NotNull Context<C> context, @NotNull CommandUsage<C> usage) throws CommandException {
        if(this.preProcessor != null){
            preProcessor.process(api, this, context, usage);
        }
    }
    
    /**
     * Sets a post-processor for the command
     *
     * @param postProcessor the post-processor for the command
     */
    @Override
    public void setPostProcessor(@NotNull CommandPostProcessor<C> postProcessor) {
        this.postProcessor = postProcessor;
    }
    
    /**
     * Executes the post-processing instructions in {@link CommandPostProcessor}
     *
     * @param api     the api
     * @param context the context
     * @param usage   the usage detected being used
     */
    @Override
    public void postProcess(@NotNull Imperat<C> api, @NotNull ResolvedContext<C> context, @NotNull CommandUsage<C> usage) throws CommandException {
        if(this.postProcessor != null) {
            this.postProcessor.process(api, this, context, usage);
        }
    }
    
    @Override
    public Type getGenericType() {
        return getType();
    }

    /**
     * Casts the parameter to a flag parameter
     *
     * @return the parameter as a flag
     */
    @Override
    public FlagParameter asFlagParameter() {
        throw new UnsupportedOperationException("A command cannot be treated as a flag !");
    }

    /**
     * Fetches the suggestion resolver linked to this
     * command parameter.
     *
     * @return the {@link SuggestionResolver} for a resolving suggestion
     */
    @Override
    @SuppressWarnings("unchecked")
    public @Nullable <CS, T> SuggestionResolver<CS, T> getSuggestionResolver() {
        return (SuggestionResolver<CS, T>) SuggestionResolver.plain(Command.class,
                List.of(this.getName()));
    }


    /**
     * @return the aliases for this commands
     */
    @Override
    public @UnmodifiableView List<String> getAliases() {
        return aliases;
    }

    /**
     * Sets the aliases of a command
     *
     * @param aliases the aliases for te command to set
     */
    @Override
    public void addAliases(List<String> aliases) {
        this.aliases.addAll(aliases);
    }

    /**
     * @return the default usage of the command
     * without any args
     */
    @Override
    public @NotNull CommandUsage<C> getDefaultUsage() {
        return defaultUsage;
    }

    /**
     * @param execution sets what happens when there are no parameters
     */
    @Override
    public void setDefaultUsageExecution(CommandExecution<C> execution) {
        this.defaultUsage = CommandUsage.<C>builder()
                .execute(execution)
                .build();
    }

    /**
     * Adds a usage to the command
     *
     * @param usage the usage {@link CommandUsage} of the command
     */
    @Override
    public void addUsage(CommandUsage<C> usage) {
        usages.add(usage);
    }

    /**
     * @return all {@link CommandUsage} that were registered
     * to this command by the user
     */
    @Override
    public Collection<? extends CommandUsage<C>> getUsages() {
        return usages;
    }

    /**
     * @return the usage that doesn't include any subcommands , only
     * parameters
     */
    @Override
    public @NotNull CommandUsage<C> getMainUsage() {
        return usages.stream().filter((usage ->
                        usage.getMinLength() >= 1 &&
                                !usage.hasParamType(Command.class)))
                .findFirst().orElse(defaultUsage);
    }

    /**
     * @return Returns {@link AutoCompleter}
     * that handles all auto-completes for this command
     */
    @Override
    public AutoCompleter<C> getAutoCompleter() {
        return autoCompleter;
    }

    /**
     * @return Whether this command is a sub command or not
     */
    @Override
    public @Nullable Command<C> getParent() {
        return parent;
    }

    /**
     * The command to be added as a subcommand of this instance
     *
     * @param command the sub-command to be added
     */
    @Override
    public void addSubCommand(Command<C> command) {
        children.put(command.getName(), command);
    }
    
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
    @Override
    public void addSubCommandUsage(
            String subCommand,
            List<String> aliases,
            CommandUsage<C> usage,
            boolean attachDirectly
    ) {
        Command<C> mapped = getSubCommand(subCommand.toLowerCase());
        if (mapped != null) {
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
    
    /**
     * @param name the name of the wanted sub-command
     * @return the sub-command of a specific name directly from
     * this instance of a command, meaning that
     * it won't go deeply in search for sub-command
     */
    @Override
    public @Nullable Command<C> getSubCommand(String name) {
        Command<C> sub = children.get(name);
        if (sub != null)
            return sub;

        for (String subsNames : children.keySet()) {
            Command<C> other = children.get(subsNames);
            if (other.hasName(name)) return other;
            else if (subsNames.startsWith(name)) return other;
        }
        return null;
    }

    /**
     * @return the subcommands of this command
     */
    @Override
    public @NotNull Collection<? extends Command<C>> getSubCommands() {
        return children.values();
    }

    /**
     * whether to ignore permission checks on the auto-completion of command and
     * sub commands or not
     *
     * @return whether to ignore permission checks on the auto-completion of command and
     * sub commands or not
     */
    @Override
    public boolean isIgnoringACPerms() {
        return suppressACPermissionChecks;
    }

    /**
     * if true, it will ignore permission checks
     * on the auto-completion of command and sub commands
     * <p>
     * otherwise, it will perform permission checks and
     * only tab-completes the usages/subcommands that you have permission for
     *
     * @param suppress true if you want to ignore the permission checks on tab completion of args
     */
    @Override
    public void ignoreACPermissions(boolean suppress) {
        this.suppressACPermissionChecks = suppress;
    }
    
    /**
     * Adds help as a sub-command to the command chain
     *
     * @param dispatcher    the api
     * @param params        the parameters of the help command
     * @param helpExecution the help execution
     */
    @Override @SuppressWarnings("unchecked")
    public void addHelpCommand(Imperat<C> dispatcher, List<CommandParameter> params, HelpExecution<C> helpExecution) {
        if (params.isEmpty() && dispatcher.getHelpTemplate() instanceof PaginatedHelpTemplate) {
            params.add(
                    CommandParameter.optionalInt("page")
                            .description("help-page")
                            .defaultValue(1)
                            .build()
            );
        }
        
        addSubCommandUsage(
                "help",
                CommandUsage.<C>builder()
                        .parameters(params)
                        .execute((sender, context) -> {
                            //CommandDebugger.debug("Executing help !");
                            Integer page = context.getArgument("page");
                            CommandHelp<C> help = dispatcher.createCommandHelp(this, (Context<C>) context);
                            helpExecution.help(sender, (Context<C>) context, help, page);
                        }).buildAsHelp(),
                true
        );
    }
    
    
}
