package dev.velix.imperat;

import dev.velix.imperat.annotations.base.AnnotationParser;
import dev.velix.imperat.annotations.base.AnnotationReader;
import dev.velix.imperat.annotations.base.AnnotationReplacer;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.processors.CommandPostProcessor;
import dev.velix.imperat.command.processors.CommandPreProcessor;
import dev.velix.imperat.command.tree.CommandDispatch;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.exception.AmbiguousUsageAdditionException;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.exception.InvalidCommandUsageException;
import dev.velix.imperat.exception.InvalidSyntaxException;
import dev.velix.imperat.exception.PermissionDeniedException;
import dev.velix.imperat.util.ImperatDebugger;
import dev.velix.imperat.util.Preconditions;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class BaseImperat<S extends Source> implements Imperat<S> {

    protected final ImperatConfig<S> config;
    private @NotNull AnnotationParser<S> annotationParser;
    private final Map<String, Command<S>> commands = new HashMap<>();

    protected BaseImperat(@NotNull ImperatConfig<S> config) {
        this.config = config;
        annotationParser = AnnotationParser.defaultParser(this);
    }

    /**
     * The config for imperat
     *
     * @return the config holding all variables.
     */
    @Override
    public ImperatConfig<S> config() {
        return config;
    }

    /**
     * Checks whether the valueType can be a command sender
     *
     * @param type the valueType
     * @return whether the valueType can be a command sender
     */
    @Override
    public boolean canBeSender(Type type) {
        return TypeWrap.of(Source.class).isSupertypeOf(type);
    }

    /**
     * Registering a command into the dispatcher
     *
     * @param command the command to register
     */
    @Override
    public void registerCommand(Command<S> command) {
        try {
            var verifier = config.getUsageVerifier();
            for (CommandUsage<S> usage : command.usages()) {
                if (!verifier.verify(usage)) throw new InvalidCommandUsageException(command, usage);

                for (CommandUsage<S> other : command.usages()) {
                    if (other.equals(usage)) continue;
                    if (verifier.areAmbiguous(usage, other))
                        throw new AmbiguousUsageAdditionException(command, usage, other);
                }
            }
            commands.put(command.name().toLowerCase(), command);
        } catch (RuntimeException ex) {
            ImperatDebugger.error(BaseImperat.class, "registerCommand(CommandProcessingChain command)", ex);
            shutdownPlatform();
        }
    }

    /**
     * Registers a command class built by the
     * annotations using a parser
     *
     * @param command the annotated command instance to parse
     */
    @Override
    @SuppressWarnings("unchecked")
    public void registerCommand(Object command) {
        if (command instanceof Command<?> commandObj) {
            this.registerCommand((Command<S>) commandObj);
            return;
        }
        annotationParser.parseCommandClass(command);
    }

    /**
     * Unregisters a command from the internal registry
     *
     * @param name the name of the command to unregister
     */
    @Override
    public void unregisterCommand(String name) {
        Preconditions.notNull(name, "commandToRemove");
        commands.remove(name.toLowerCase());
    }

    /**
     * Unregisters all commands from the internal registry
     */
    @Override
    public void unregisterAllCommands() {
        commands.clear();
    }

    /**
     * @param name the name/alias of the command
     * @return fetches {@link Command} with specific name/alias
     */
    @Override
    public @Nullable Command<S> getCommand(final String name) {
        final String cmdName = name.toLowerCase();
        final Command<S> result = commands.get(cmdName);

        if (result != null) return result;
        for (Command<S> headCommands : commands.values()) {
            if (headCommands.hasName(cmdName)) return headCommands;
        }

        return null;
    }


    /**
     * Changes the instance of {@link AnnotationParser}
     *
     * @param parser the parser
     */
    @Override
    public void setAnnotationParser(AnnotationParser<S> parser) {
        Preconditions.notNull(parser, "Parser");
        this.annotationParser = parser;
    }

    /**
     * Registers a valueType of annotations so that it can be
     * detected by {@link AnnotationReader} , it's useful as it allows that valueType of annotation
     * to be recognized as a true Imperat-related annotation to be used in something like checking if a
     * {@link CommandParameter} is annotated and checks for the annotations it has.
     *
     * @param type the valueType of annotation
     */
    @SafeVarargs
    @Override
    public final void registerAnnotations(Class<? extends Annotation>... type) {
        annotationParser.registerAnnotations(type);
    }

    /**
     * Registers {@link AnnotationReplacer}
     *
     * @param type     the valueType to replace the annotation by
     * @param replacer the replacer
     */
    @Override
    public <A extends Annotation> void registerAnnotationReplacer(Class<A> type, AnnotationReplacer<A> replacer) {
        annotationParser.registerAnnotationReplacer(type, replacer);
    }


    /**
     * @param owningCommand the command owning this sub-command
     * @param name          the name of the subcommand you're looking for
     * @return the subcommand of a command
     */
    @Override
    public @Nullable Command<S> getSubCommand(String owningCommand, String name) {
        Command<S> owningCmd = getCommand(owningCommand);
        if (owningCmd == null) return null;

        for (Command<S> subCommand : owningCmd.getSubCommands()) {
            Command<S> result = search(subCommand, name);
            if (result != null) return result;
        }

        return null;
    }


    @ApiStatus.Internal
    private Command<S> search(Command<S> sub, String name) {
        if (sub.hasName(name)) {
            return sub;
        }

        for (Command<S> other : sub.getSubCommands()) {

            if (other.hasName(name)) {
                return other;
            } else {
                return search(other, name);
            }
        }

        return null;
    }

    @Override
    public @NotNull CommandDispatch.Result dispatch(Context<S> context) {
        try {
            return handleExecution(context);
        } catch (Throwable ex) {
            config.handleExecutionThrowable(ex, context, BaseImperat.class, "dispatch");
            return CommandDispatch.Result.UNKNOWN;
        }
    }

    @Override
    public @NotNull CommandDispatch.Result dispatch(S source, Command<S> command, String[] rawInput) {
        ArgumentQueue rawArguments = ArgumentQueue.parse(rawInput);

        Context<S> plainContext = config.getContextFactory()
            .createContext(this, source, command, rawArguments);

        return dispatch(plainContext);
    }

    @Override
    public @NotNull CommandDispatch.Result dispatch(S source, String commandName, String[] rawInput) {
        Command<S> command = getCommand(commandName);
        if (command == null) {
            source.error("Unknown command input: '" + commandName + "'");
            return CommandDispatch.Result.UNKNOWN;
        }
        command.visualizeTree();
        return dispatch(source, command, rawInput);
    }

    @Override
    public @NotNull CommandDispatch.Result dispatch(S sender, String commandName, String rawArgsOneLine) {
        return dispatch(sender, commandName, rawArgsOneLine.split(" "));
    }

    @Override
    public CommandDispatch.Result dispatch(S sender, String line) {
        String[] lineArgs = line.split(" ");
        String[] argumentsOnly = new String[lineArgs.length - 1];
        System.arraycopy(lineArgs, 1, argumentsOnly, 0, lineArgs.length - 1);
        return dispatch(sender, lineArgs[0], argumentsOnly);
    }

    private CommandDispatch.Result handleExecution(Context<S> context) throws ImperatException {
        Command<S> command = context.command();
        S source = context.source();

        if (!config.getPermissionResolver().hasPermission(source, command.permission())) {
            throw new PermissionDeniedException();
        }

        if (context.arguments().isEmpty()) {
            CommandUsage<S> defaultUsage = command.getDefaultUsage();
            executeUsage(command, source, context, defaultUsage);
            return CommandDispatch.Result.INCOMPLETE;
        }

        CommandDispatch<S> searchResult = command.contextMatch(context);
        searchResult.visualize();

        CommandUsage<S> usage = searchResult.toUsage(command);

        //executing usage
        if (searchResult.result() == CommandDispatch.Result.COMPLETE) {
            ImperatDebugger.debug("Executing usage '%s'", usage != null ? CommandUsage.format(command, usage) : "NULL");
            executeUsage(command, source, context, usage);
        }
        else if (searchResult.result() == CommandDispatch.Result.INCOMPLETE) {
            if(searchResult.getLastParameter().isCommand()) {
                ImperatDebugger.debug("Executing last parameter as a sub command");
                var defUsage = searchResult.getLastParameter().asCommand().getDefaultUsage();
                executeUsage(command, source, context, defUsage);
            }
            else if (usage != null) {
                ImperatDebugger.debug("Executing usage '%s'", CommandUsage.format(command, usage));
                executeUsage(command, source, context, usage);
            } else {
                //usage is null and last param is not command
                throw new InvalidSyntaxException();
            }
        } else {
            throw new InvalidSyntaxException();
        }
        return searchResult.result();
    }

    private void executeUsage(
        final Command<S> command,
        final S source,
        final Context<S> context,
        final CommandUsage<S> usage
    ) throws ImperatException {
        //global pre-processing
        preProcess(context, usage);

        //per command pre-processing
        command.preProcess(this, context, usage);

        ResolvedContext<S> resolvedContext = config.getContextFactory().createResolvedContext(context, usage);
        resolvedContext.resolve();
        resolvedContext.debug();

        //global post-processing
        postProcess(resolvedContext);

        //per command post-processing
        command.postProcess(this, resolvedContext, usage);

        //executing the usage
        usage.execute(this, source, resolvedContext);
    }

    private void preProcess(
        @NotNull Context<S> context,
        @NotNull CommandUsage<S> usage
    ) {

        for (CommandPreProcessor<S> preProcessor : config.getPreProcessors()) {
            try {
                preProcessor.process(this, context, usage);
            } catch (Throwable ex) {
                config.handleExecutionThrowable(
                    ex,
                    context, preProcessor.getClass(),
                    "CommandPreProcessor#process"
                );
                break;
            }
        }
    }

    private void postProcess(
        @NotNull ResolvedContext<S> context
    ) {
        for (CommandPostProcessor<S> postProcessor : config.getPostProcessors()) {
            try {
                postProcessor.process(this, context);
            } catch (Throwable ex) {
                config.handleExecutionThrowable(
                    ex,
                    context, postProcessor.getClass(),
                    "CommandPostProcessor#process"
                );
                break;
            }
        }
    }

    /**
     * @param command the data about the command being written in the chat box
     * @param source  the sender writing the command
     * @param args    the arguments currently written
     * @return the suggestions at the current position
     */
    @Override
    public CompletableFuture<List<String>> autoComplete(Command<S> command, S source, String[] args) {
        return command.autoCompleter().autoComplete(this, source, args);
    }

    /**
     * Gets all registered commands
     *
     * @return the registered commands
     */
    @Override
    public Collection<? extends Command<S>> getRegisteredCommands() {
        return commands.values();
    }

    @Override
    public void debug(boolean treeVisualizing) {
        for (var cmd : commands.values()) {
            if (treeVisualizing) {
                cmd.visualizeTree();
            } else {
                ImperatDebugger.debug("Debugging command '%s'", cmd.name());
                for (CommandUsage<S> usage : cmd.usages()) {
                    ImperatDebugger.debug("   - '%s'", CommandUsage.format(cmd, usage));
                }
            }
        }
    }

}
