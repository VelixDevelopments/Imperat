package dev.velix.imperat.command;

import dev.velix.imperat.command.processors.CommandPostProcessor;
import dev.velix.imperat.command.processors.CommandPreProcessor;
import dev.velix.imperat.command.processors.impl.UsageCooldownProcessor;
import dev.velix.imperat.command.processors.impl.UsagePermissionProcessor;
import dev.velix.imperat.exceptions.ExecutionFailure;
import dev.velix.imperat.command.tree.Traverse;
import dev.velix.imperat.command.tree.TraverseResult;
import dev.velix.imperat.util.CommandDebugger;
import dev.velix.imperat.util.CommandExceptionHandler;
import dev.velix.imperat.Imperat;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.annotations.AnnotationParser;
import dev.velix.imperat.annotations.AnnotationReplacer;
import dev.velix.imperat.caption.Caption;
import dev.velix.imperat.caption.CaptionKey;
import dev.velix.imperat.caption.CaptionRegistry;
import dev.velix.imperat.command.suggestions.SuggestionResolverRegistry;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.context.internal.ContextFactory;
import dev.velix.imperat.context.internal.ContextResolverFactory;
import dev.velix.imperat.context.internal.ContextResolverRegistry;
import dev.velix.imperat.context.internal.ValueResolverRegistry;
import dev.velix.imperat.exceptions.AmbiguousUsageAdditionException;
import dev.velix.imperat.exceptions.CommandException;
import dev.velix.imperat.exceptions.InvalidCommandUsageException;
import dev.velix.imperat.help.HelpTemplate;
import dev.velix.imperat.help.templates.DefaultTemplate;
import dev.velix.imperat.resolvers.ContextResolver;
import dev.velix.imperat.resolvers.PermissionResolver;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.resolvers.ValueResolver;
import dev.velix.imperat.util.Preconditions;
import dev.velix.imperat.verification.UsageVerifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;

@ApiStatus.Internal
public abstract class BaseImperat<C> implements Imperat<C> {

    public final static String START_PREFIX = "<dark_gray><bold>[<gold>!</gold>]</bold></dark_gray> ";
    public final static String FULL_SYNTAX_PREFIX = START_PREFIX + "<dark_aqua>Full syntax:</dark_aqua> ";

    public final static HelpTemplate DEFAULT_HELP_TEMPLATE = new DefaultTemplate();

    private final Map<String, Command<C>> commands = new HashMap<>();

    private ContextFactory<C> contextFactory;

    private final ContextResolverRegistry<C> contextResolverRegistry;

    private final ValueResolverRegistry<C> valueResolverRegistry;

    private final SuggestionResolverRegistry<C> suggestionResolverRegistry;

    private final List<CommandPreProcessor<C>> globalPreProcessors = new ArrayList<>();
    private final List<CommandPostProcessor<C>> globalPostProcessors = new ArrayList<>();
    
    private @NotNull UsageVerifier<C> verifier;

    private final CaptionRegistry<C> captionRegistry;

    private HelpTemplate template = DEFAULT_HELP_TEMPLATE;

    private AnnotationParser<C> annotationParser;

    protected PermissionResolver<C> permissionResolver;
    
    
    protected BaseImperat(PermissionResolver<C> permissionResolver) {
        contextFactory = ContextFactory.defaultFactory();
        contextResolverRegistry = ContextResolverRegistry.createDefault();
        valueResolverRegistry = ValueResolverRegistry.createDefault();
        suggestionResolverRegistry = SuggestionResolverRegistry.createDefault();
        verifier = UsageVerifier.defaultVerifier();
        captionRegistry = CaptionRegistry.createDefault();
        annotationParser = AnnotationParser.defaultParser(this);
        this.permissionResolver = permissionResolver;
        registerProcessors();
    }
    
    private void registerProcessors() {
        registerGlobalPreProcessor(new UsagePermissionProcessor<>());
        registerGlobalPreProcessor(new UsageCooldownProcessor<>());
        //TODO register creative built-in processors in the future
    }

    /**
     * Registering a command into the dispatcher
     *
     * @param command the command to register
     */
    @Override
    public void registerCommand(Command<C> command) {
        try {
            for (CommandUsage<C> usage : command.getUsages()) {
                if (!verifier.verify(usage))
                    throw new InvalidCommandUsageException(command, usage);

                for (CommandUsage<C> other : command.getUsages()) {
                    if (other.equals(usage)) continue;
                    if (verifier.areAmbiguous(usage, other))
                        throw new AmbiguousUsageAdditionException(command, usage, other);
                }

            }
            commands.put(command.getName().toLowerCase(), command);
        } catch (RuntimeException ex) {
            CommandDebugger.error(BaseImperat.class, "registerCommand(Command command)", ex);
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
    public void registerCommand(Object command) {
        Command<C> parsedCommand = annotationParser.parseCommandClass(command);
        registerCommand(parsedCommand);
    }

    /**
     * @param name the name/alias of the command
     * @return fetches {@link Command} with specific name/alias
     */
    @Override
    public @Nullable Command<C> getCommand(final String name) {
        final String cmdName = name.toLowerCase();

        Command<C> result = commands.get(cmdName);
        if (result != null) return result;
        for (Command<C> headCommands : commands.values()) {
            if (headCommands.hasName(cmdName)) return headCommands;
        }

        return null;
    }

    /**
     * @return {@link PermissionResolver} for the dispatcher
     */
    @Override
    public PermissionResolver<C> getPermissionResolver() {
        return permissionResolver;
    }

    /**
     * Changes the instance of {@link AnnotationParser}
     *
     * @param parser the parser
     */
    @Override
    public void setAnnotationParser(AnnotationParser<C> parser) {
        Preconditions.notNull(parser, "Parser cannot be null !");
        this.annotationParser = parser;
    }

    /**
     * Registers {@link AnnotationReplacer}
     *
     * @param type     the type to replace the annotation by
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
    public @Nullable Command<C> getSubCommand(String owningCommand, String name) {
        Command<C> owningCmd = getCommand(owningCommand);
        if (owningCmd == null) return null;

        for (Command<C> subCommand : owningCmd.getSubCommands()) {
            Command<C> result = search(subCommand, name);
            if (result != null) return result;
        }

        return null;
    }

    /**
     * @return the factory for creation of
     * command related contexts {@link Context}
     */
    @Override
    public ContextFactory<C> getContextFactory() {
        return contextFactory;
    }

    /**
     * sets the context factory {@link ContextFactory} for the contexts
     *
     * @param contextFactory the context factory to set
     */
    @Override
    public void setContextFactory(ContextFactory<C> contextFactory) {
        this.contextFactory = contextFactory;
    }


    /**
     * Checks whether the type has
     * a registered context-resolver
     *
     * @param type the type
     * @return whether the type has
     * a context-resolver
     */
    @Override
    public boolean hasContextResolver(Type type) {
        return getContextResolver(type) != null;
    }

    /**
     * Registers a context resolver factory
     *
     * @param factory the factory to register
     */
    @Override
    public void registerContextResolverFactory(ContextResolverFactory<C> factory) {
        contextResolverRegistry.setFactory(factory);
    }

    /**
     * @return returns the factory for creation of
     * {@link ContextResolver}
     */
    @Override
    public ContextResolverFactory<C> getContextResolverFactory() {
        return contextResolverRegistry.getFactory();
    }

    /**
     * Fetches {@link ContextResolver} for a certain type
     *
     * @param resolvingContextType the type for this resolver
     * @return the context resolver
     */
    @Override
    public <T> @Nullable ContextResolver<C, T> getContextResolver(Type resolvingContextType) {
        return contextResolverRegistry.getResolver(resolvingContextType);
    }

    /**
     * Registers {@link ContextResolver}
     *
     * @param type     the class-type of value being resolved from context
     * @param resolver the resolver for this value
     */
    @Override
    public <T> void registerContextResolver(Type type,
                                            @NotNull ContextResolver<C, T> resolver) {
        contextResolverRegistry.registerResolver(type, resolver);
    }


    /**
     * Registers {@link ValueResolver}
     *
     * @param type     the class-type of value being resolved from context
     * @param resolver the resolver for this value
     */
    @Override
    public <T> void registerValueResolver(Type type, @NotNull ValueResolver<C, T> resolver) {
        valueResolverRegistry.registerResolver(type, resolver);
    }

    /**
     * @return all currently registered {@link ValueResolver}
     */
    @Override
    public Collection<? extends ValueResolver<C, ?>> getRegisteredValueResolvers() {
        return valueResolverRegistry.getAll();
    }

    /**
     * Fetches {@link ValueResolver} for a certain value
     *
     * @param resolvingValueType the value that the resolver ends providing it from the context
     * @return the context resolver of a certain type
     */
    @Override
    public @Nullable <T> ValueResolver<C, T> getValueResolver(Type resolvingValueType) {
        return valueResolverRegistry.getResolver(resolvingValueType);
    }


    /**
     * Fetches the suggestion provider/resolver for a specific type of
     * argument or parameter.
     *
     * @param clazz the clazz symbolizing the type
     * @return the {@link SuggestionResolver} instance for that type
     */
    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <T> SuggestionResolver<C, T> getSuggestionResolverByType(Class<T> clazz) {
        return (SuggestionResolver<C, T>) suggestionResolverRegistry.getResolver(clazz);
    }

    /**
     * Checks whether the type can be a command sender
     *
     * @param type the type
     * @return whether the type can be a command sender
     */
    @Override
    public boolean canBeSender(Class<?> type) {
        return Source.class.isAssignableFrom(type);
    }

    /**
     * Registers a suggestion resolver
     *
     * @param suggestionResolver the suggestion resolver to register
     */
    @Override
    public <T> void registerSuggestionResolver(SuggestionResolver<C, T> suggestionResolver) {
        suggestionResolverRegistry.registerResolver(suggestionResolver);
    }

    /**
     * Fetches the suggestion provider/resolver for a specific argument
     *
     * @param name the name of the argument
     * @return the {@link SuggestionResolver} instance for that argument
     */
    public @Nullable <T> SuggestionResolver<C, T> getNamedSuggestionResolver(String name) {
        return suggestionResolverRegistry.getResolverByName(name);
    }

    /**
     * Registers a suggestion resolver linked
     * directly to a unique name
     *
     * @param name               the unique name/id of the suggestion resolver
     * @param suggestionResolver the suggestion resolver to register
     */
    @Override
    public <T> void registerNamedSuggestionResolver(String name, SuggestionResolver<C, T> suggestionResolver) {
        suggestionResolverRegistry.registerNamedResolver(name, suggestionResolver);
    }

    /**
     * Sets the usage verifier to a new instance
     *
     * @param usageVerifier the usage verifier to set
     */
    @Override
    public void setUsageVerifier(UsageVerifier<C> usageVerifier) {
        this.verifier = usageVerifier;
    }


    @ApiStatus.Internal
    private Command<C> search(Command<C> sub, String name) {
        if (sub.hasName(name)) {
            return sub;
        }

        for (Command<C> other : sub.getSubCommands()) {

            if (other.hasName(name)) {
                return other;
            } else {
                return search(other, name);
            }
        }

        return null;
    }
    
    /**
     * Registers a command pre-processor
     *
     * @param preProcessor the pre-processor to register
     */
    @Override
    public void registerGlobalPreProcessor(CommandPreProcessor<C> preProcessor) {
        Preconditions.notNull(preProcessor, "Pre-processor cannot be null");
        globalPreProcessors.add(preProcessor);
    }
    
    /**
     * Registers a command post-processor
     *
     * @param postProcessor the post-processor to register
     */
    @Override
    public void registerGlobalPostProcessor(CommandPostProcessor<C> postProcessor) {
        Preconditions.notNull(postProcessor, "Post-processor cannot be null");
        globalPostProcessors.add(postProcessor);
    }
    
    /**
     * Registers a command pre-processor
     *
     * @param priority     the priority for the processor
     * @param preProcessor the pre-processor to register
     */
    @Override
    public void registerGlobalPreProcessor(int priority, CommandPreProcessor<C> preProcessor) {
        Preconditions.notNull(preProcessor, "Pre-processor cannot be null");
        globalPreProcessors.add(priority, preProcessor);
    }
    
    /**
     * Registers a command post-processor
     *
     * @param priority      the priority for the processor
     * @param postProcessor the post-processor to register
     */
    @Override
    public void registerGlobalPostProcessor(int priority, CommandPostProcessor<C> postProcessor) {
        Preconditions.notNull(postProcessor, "Post-processor cannot be null");
        globalPostProcessors.add(priority, postProcessor);
    }
    
    /**
     * Registers a caption
     *
     * @param caption the caption to register
     */
    @Override
    public void registerCaption(Caption<C> caption) {
        Preconditions.notNull(caption, "Caption cannot be null");
        this.captionRegistry.registerCaption(caption);
    }

    /**
     * Sends a caption to the source
     *
     * @param key     the id/key of the caption
     * @param context the context of the command
     */
    @Override
    public void sendCaption(CaptionKey key,
                            Context<C> context) {
        this.sendCaption(key, context, null);
    }

    /**
     * Sends a caption to the source
     *
     * @param key       the id/key of the caption
     * @param context   the context of the command
     * @param exception the error if present
     */
    @Override
    public void sendCaption(CaptionKey key,
                            Context<C> context,
                            @Nullable Exception exception) {
        Caption<C> caption = captionRegistry.getCaption(key);
        if (caption == null) {
            throw new IllegalStateException(String.format("Unregistered caption from key '%s'", key.id()));
        }
        Source<C> source = context.getSource();
        source.reply(caption, context);
    }
    
    /**
     * Fetches the caption from a caption key
     *
     * @param key the key
     * @return the caption to get
     */
    @Override
    public @Nullable Caption<C> getCaption(@NotNull CaptionKey key) {
        return captionRegistry.getCaption(key);
    }
    
    /**
     * Dispatches and executes a command with certain raw arguments
     *
     * @param sender      the sender/executor of this command
     * @param commandName the name of the command to execute
     * @param rawInput    the command's args input
     */
    @Override
    public void dispatch(C sender, String commandName, String... rawInput) {

        ArgumentQueue rawArguments = ArgumentQueue.parse(rawInput);
        //CommandDebugger.visualize("Raw input = '%s'", String.join(",", rawArguments));
        Source<C> source = wrapSender(sender);
        
        Command<C> command = getCommand(commandName);
        if (command == null) {
            source.reply("Unknown command !");
            return;
        }
        
        Context<C> plainContext = getContextFactory()
                .createContext(this, source, command, rawArguments);

        try {
            handleExecution(source, plainContext);
        } catch (Throwable ex) {
            CommandExceptionHandler.handleException(this, plainContext, BaseImperat.class, "dispatch", ex);
        }

    }

    @Override
    public void dispatch(C sender, String commandName, String rawArgsOneLine) {
        dispatch(sender, commandName, rawArgsOneLine.split(" "));
    }

    private void handleExecution(Source<C> source, Context<C> context) throws CommandException {
        Command<C> command = context.getCommandUsed();
        if (!getPermissionResolver().hasPermission(source, command.getPermission())) {
            throw new ExecutionFailure(CaptionKey.NO_PERMISSION);
        }

        if (context.getArguments().isEmpty()) {
            CommandUsage<C> defaultUsage = command.getDefaultUsage();
            defaultUsage.execute(this, source, context);
            return;
        }
        
        Traverse searchResult = command.traverse(context);

        //executing usage
        CommandUsage<C> usage = searchResult.toUsage(command);
        
        if (searchResult.result() == TraverseResult.COMPLETE)
            executeUsage(command, source, context, usage);
        else if(searchResult.result() == TraverseResult.INCOMPLETE) {
            var lastParameter = searchResult.getLastParameter();
            if(lastParameter.isCommand()) {
                Command<C> sub = lastParameter.asCommand();
                CommandUsage<C> defaultUsage = sub.getDefaultUsage();
                executeUsage(command, source, context, defaultUsage);
            }else {
                throw new ExecutionFailure(CaptionKey.INVALID_SYNTAX);
            }
        }
        else
            throw new ExecutionFailure(CaptionKey.INVALID_SYNTAX);
    }

    private void executeUsage(Command<C> command,
                              Source<C> source,
                              Context<C> context,
                              final CommandUsage<C> usage) throws CommandException {
        
        //global pre-processing
        preProcess(command, context, usage);
        
        //per command pre-processing
        command.preProcess(this, context, usage);
        
        ResolvedContext<C> resolvedContext = contextFactory.createResolvedContext(this, command, context, usage);
        resolvedContext.resolve();
        
        //global post-processing
        postProcess(command, resolvedContext, usage);
        
        //per command post-processing
        command.postProcess(this, resolvedContext, usage);
        
        usage.execute(this, source, resolvedContext);
    }
    
    //TODO improve (DRY)
    private void preProcess(
            @NotNull Command<C> command,
            @NotNull Context<C> context,
            @NotNull CommandUsage<C> usage
    ) {
        for(CommandPreProcessor<C> preProcessor : globalPreProcessors) {
            try {
                preProcessor.process(this, command, context, usage);
            }catch (Throwable ex) {
                CommandExceptionHandler.handleException(
                        this,
                        context, preProcessor.getClass(),
                        "CommandPreProcessor#process", ex
                );
                break;
            }
        }
    }
    
    //TODO improve (DRY)
    private void postProcess(
            @NotNull Command<C> command,
            @NotNull ResolvedContext<C> context,
            @NotNull CommandUsage<C> usage
    ) {
        for(CommandPostProcessor<C> postProcessor : globalPostProcessors) {
            try {
                postProcessor.process(this, command, context, usage);
            }catch (Throwable ex) {
                CommandExceptionHandler.handleException(
                        this,
                        context, postProcessor.getClass(),
                        "CommandPostProcessor#process", ex
                );
                break;
            }
        }
    }

    /**
     * @param command the data about the command being written in the chat box
     * @param sender  the sender writing the command
     * @param args    the arguments currently written
     * @return the suggestions at the current position
     */
    @Override
    public List<String> autoComplete(Command<C> command, C sender, String[] args) {
        Source<C> source = wrapSender(sender);
        return command.getAutoCompleter().autoComplete(this, source, args);
    }

    /**
     * Gets all registered commands
     *
     * @return the registered commands
     */
    @Override
    public Collection<? extends Command<C>> getRegisteredCommands() {
        return commands.values();
    }

    /**
     * @return The template for showing help
     */
    @Override
    public @NotNull HelpTemplate getHelpTemplate() {
        return template;
    }

    /**
     * Set the help template to use
     *
     * @param template the help template
     */
    @Override
    public void setHelpTemplate(HelpTemplate template) {
        this.template = template;
    }


}
