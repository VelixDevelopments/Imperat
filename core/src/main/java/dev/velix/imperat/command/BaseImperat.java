package dev.velix.imperat.command;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.base.AnnotationParser;
import dev.velix.imperat.annotations.base.AnnotationReader;
import dev.velix.imperat.annotations.base.AnnotationReplacer;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.processors.CommandPostProcessor;
import dev.velix.imperat.command.processors.CommandPreProcessor;
import dev.velix.imperat.command.processors.impl.UsageCooldownProcessor;
import dev.velix.imperat.command.processors.impl.UsagePermissionProcessor;
import dev.velix.imperat.command.suggestions.SuggestionResolverRegistry;
import dev.velix.imperat.command.tree.CommandDispatch;
import dev.velix.imperat.command.tree.UsageMatchResult;
import dev.velix.imperat.context.*;
import dev.velix.imperat.context.internal.ContextFactory;
import dev.velix.imperat.exception.*;
import dev.velix.imperat.help.HelpProvider;
import dev.velix.imperat.placeholders.Placeholder;
import dev.velix.imperat.placeholders.PlaceholderRegistry;
import dev.velix.imperat.resolvers.*;
import dev.velix.imperat.util.ImperatDebugger;
import dev.velix.imperat.util.Preconditions;
import dev.velix.imperat.util.TypeWrap;
import dev.velix.imperat.verification.UsageVerifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;

public abstract class BaseImperat<S extends Source> implements Imperat<S> {

    
    private final ContextResolverRegistry<S> contextResolverRegistry;
    private final ValueResolverRegistry<S> valueResolverRegistry;
    private final SuggestionResolverRegistry<S> suggestionResolverRegistry;
    private final PlaceholderRegistry<S> placeholderRegistry;
    private final SourceResolverRegistry<S> sourceResolverRegistry;
    
    protected @NotNull PermissionResolver<S> permissionResolver;
    private @NotNull ContextFactory<S> contextFactory;
    private @NotNull UsageVerifier<S> verifier;
    private @Nullable HelpProvider<S> provider = null;
    private @NotNull AnnotationParser<S> annotationParser;
    
    private final Map<String, Command<S>> commands = new HashMap<>();
    private final List<CommandPreProcessor<S>> globalPreProcessors = new ArrayList<>();
    private final List<CommandPostProcessor<S>> globalPostProcessors = new ArrayList<>();
    private final Map<Class<? extends Throwable>, ThrowableResolver<?, S>> handlers = new HashMap<>();
    
    protected BaseImperat(@NotNull PermissionResolver<S> permissionResolver) {
        contextFactory = ContextFactory.defaultFactory();
        contextResolverRegistry = ContextResolverRegistry.createDefault(this);
        valueResolverRegistry = ValueResolverRegistry.createDefault();
        suggestionResolverRegistry = SuggestionResolverRegistry.createDefault();
        sourceResolverRegistry = SourceResolverRegistry.createDefault();
        placeholderRegistry = PlaceholderRegistry.createDefault(this);
        verifier = UsageVerifier.typeTolerantVerifier();
        this.permissionResolver = permissionResolver;
        this.registerProcessors();
        this.regDefThrowableResolvers();
        annotationParser = AnnotationParser.defaultParser(this);
    }
    
    private void registerProcessors() {
        registerGlobalPreProcessor(new UsagePermissionProcessor<>());
        registerGlobalPreProcessor(new UsageCooldownProcessor<>());
        //TODO register creative built-in processors in the future
    }

    private void regDefThrowableResolvers() {
        this.setThrowableResolver(InvalidSourceException.class, (exception, imperat, context) -> {
            throw new UnsupportedOperationException("Couldn't find any source resolver for type `"
                    + exception.getTargetType().getType().getTypeName() + "'");
        });
        
        this.setThrowableResolver(
                SourceException.class,
                (exception, imperat, context) -> {
                    final String msg = exception.getMessage();
                    switch (exception.getType()) {
                        case SEVERE -> context.source().error(msg);
                        case WARN -> context.source().warn(msg);
                        case REPLY -> context.source().reply(msg);
                    }
                }
        );
        
        this.setThrowableResolver(
                InvalidUUIDException.class, (exception, imperat, context) ->
                        context.source().error("Invalid uuid-format '" + exception.getRaw() + "'")
        );
        
        this.setThrowableResolver(
                CooldownException.class,
                (exception, imperat, context) -> {
                    final long lastTimeExecuted = exception.getCooldown();
                    final long timePassed = System.currentTimeMillis() - lastTimeExecuted;
                    final long remaining = exception.getDefaultCooldown() - timePassed;
                    context.source().error(
                            "Please wait %d second(s) to execute this command again!".formatted(remaining)
                    );
                }
        );
        this.setThrowableResolver(
                PermissionDeniedException.class,
                (exception, imperat, context) -> context.source().error("You don't have permission to use this command!")
        );
        this.setThrowableResolver(
                InvalidSyntaxException.class,
                (exception, imperat, context) -> {
                    S source = context.source();
                    if (!(context instanceof ResolvedContext<S> resolvedContext) || resolvedContext.getDetectedUsage() == null) {
                        source.error(
                                "Unknown command, usage '<raw_args>' is unknown.".replace("<raw_args>", context.arguments().join(" "))
                        );
                        return;
                    }
                    
                    var usage = resolvedContext.getDetectedUsage();
                    final int last = context.arguments().size() - 1;

                    final List<CommandParameter<S>> params = new ArrayList<>(usage.getParameters())
                            .stream()
                            .filter((param) -> !param.isOptional() && param.position() > last)
                            .toList();

                    final StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < params.size(); i++) {
                        CommandParameter<S> param = params.get(i);
                        assert !param.isOptional();
                        builder.append(param.format());
                        if (i != params.size() - 1)
                            builder.append(' ');

                    }
                    //INCOMPLETE USAGE, AKA MISSING REQUIRED INPUTS
                    source.error(
                            "Missing required arguments '<required_args>'\n Full syntax: '<usage>'"
                                    .replace("<required_args>", builder.toString())
                                    .replace("<usage>", imperat.commandPrefix()
                                            + CommandUsage.format(resolvedContext.command(), usage))
                    );
                }
        );
        this.setThrowableResolver(
                NoHelpException.class,
                (exception, imperat, context) -> {
                    Command<S> cmdUsed;
                    if (context instanceof ResolvedContext<S> resolvedContext) {
                        cmdUsed = resolvedContext.getLastUsedCommand();
                    } else {
                        cmdUsed = imperat.getCommand(context.command());
                    }
                    assert cmdUsed != null;
                    context.source().error("No Help available for '<command>'".replace("<command>", cmdUsed.name()));
                }
        );
        this.setThrowableResolver(
                NoHelpPageException.class,
                (exception, imperat, context) -> {
                    if (!(context instanceof ResolvedContext<S> resolvedContext) || resolvedContext.getDetectedUsage() == null
                            || resolvedContext.getDetectedUsage().isHelp()) {
                        throw new IllegalCallerException("Called NoHelpPageCaption in wrong the wrong sequence/part of the code");
                    }
                    
                    int page = resolvedContext.getArgumentOr("page", 1);
                    context.source().error("Page '<page>' doesn't exist!".replace("<page>", String.valueOf(page)));
                }
        );
    }

    /**
     * Registering a command into the dispatcher
     *
     * @param command the command to register
     */
    @Override
    public void registerCommand(Command<S> command) {
        try {
            for (CommandUsage<S> usage : command.usages()) {
                if (!verifier.verify(usage))
                    throw new InvalidCommandUsageException(command, usage);
                
                for (CommandUsage<S> other : command.usages()) {
                    if (other.equals(usage)) continue;
                    if (verifier.areAmbiguous(usage, other))
                        throw new AmbiguousUsageAdditionException(command, usage, other);
                }
                
            }
            commands.put(command.name().toLowerCase(), command);
        } catch (RuntimeException ex) {
            ImperatDebugger.error(BaseImperat.class, "registerCommand(Command command)", ex);
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
     * @param name the name/alias of the command
     * @return fetches {@link Command} with specific name/alias
     */
    @Override
    public @Nullable Command<S> getCommand(final String name) {
        final String cmdName = name.toLowerCase();
        
        Command<S> result = commands.get(cmdName);
        if (result != null) return result;
        for (Command<S> headCommands : commands.values()) {
            if (headCommands.hasName(cmdName)) return headCommands;
        }
        
        return null;
    }
    
    /**
     * @return {@link PermissionResolver} for the dispatcher
     */
    @Override
    public @NotNull PermissionResolver<S> getPermissionResolver() {
        return permissionResolver;
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
     * Registers a type of annotations so that it can be
     * detected by {@link AnnotationReader} , it's useful as it allows that type of annotation
     * to be recognized as a true Imperat-related annotation to be used in something like checking if a
     * {@link CommandParameter} is annotated and checks for the annotations it has.
     *
     * @param type the type of annotation
     */
    @SafeVarargs
    @Override
    public final void registerAnnotations(Class<? extends Annotation>... type) {
        annotationParser.registerAnnotations(type);
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
    public @Nullable Command<S> getSubCommand(String owningCommand, String name) {
        Command<S> owningCmd = getCommand(owningCommand);
        if (owningCmd == null) return null;
        
        for (Command<S> subCommand : owningCmd.getSubCommands()) {
            Command<S> result = search(subCommand, name);
            if (result != null) return result;
        }
        
        return null;
    }
    
    /**
     * @return the factory for creation of
     * command related contexts {@link Context}
     */
    @Override
    public @NotNull ContextFactory<S> getContextFactory() {
        return contextFactory;
    }
    
    /**
     * sets the context factory {@link ContextFactory} for the contexts
     *
     * @param contextFactory the context factory to set
     */
    @Override
    public void setContextFactory(@NotNull ContextFactory<S> contextFactory) {
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
    public void registerContextResolverFactory(ContextResolverFactory<S> factory) {
        contextResolverRegistry.setFactory(factory);
    }
    
    /**
     * @return returns the factory for creation of
     * {@link ContextResolver}
     */
    @Override
    public ContextResolverFactory<S> getContextResolverFactory() {
        return contextResolverRegistry.getFactory();
    }
    
    /**
     * Fetches {@link ContextResolver} for a certain type
     *
     * @param resolvingContextType the type for this resolver
     * @return the context resolver
     */
    @Override
    public <T> @Nullable ContextResolver<S, T> getContextResolver(Type resolvingContextType) {
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
                                            @NotNull ContextResolver<S, T> resolver) {
        contextResolverRegistry.registerResolver(type, resolver);
    }
    
    /**
     * Registers {@link ValueResolver}
     *
     * @param type     the class-type of value being resolved from context
     * @param resolver the resolver for this value
     */
    @Override
    public <T> void registerValueResolver(Type type, @NotNull ValueResolver<S, T> resolver) {
        valueResolverRegistry.registerResolver(type, resolver);
    }
    
    /**
     * @return all currently registered {@link ValueResolver}
     */
    @Override
    public Collection<? extends ValueResolver<S, ?>> getRegisteredValueResolvers() {
        return valueResolverRegistry.getAll();
    }
    
    /**
     * Fetches {@link ValueResolver} for a certain value
     *
     * @param resolvingValueType the value that the resolver ends providing it from the context
     * @return the context resolver of a certain type
     */
    @Override
    public @Nullable ValueResolver<S, ?> getValueResolver(Type resolvingValueType) {
        return valueResolverRegistry.getResolver(resolvingValueType);
    }
    
    /**
     * Fetches the suggestion provider/resolver for a specific type of
     * argument or parameter.
     *
     * @param type the type
     * @return the {@link SuggestionResolver} instance for that type
     */
    @Override
    public @Nullable SuggestionResolver<S> getSuggestionResolverByType(Type type) {
        return suggestionResolverRegistry.getResolver(type);
    }
    
    /**
     * Checks whether the type can be a command sender
     *
     * @param type the type
     * @return whether the type can be a command sender
     */
    @Override
    public boolean canBeSender(Type type) {
        return TypeWrap.of(Source.class).isSupertypeOf(type);
    }
    
    /**
     * Registers a suggestion resolver
     *
     * @param suggestionResolver the suggestion resolver to register
     */
    @Override
    public <T> void registerSuggestionResolver(TypeSuggestionResolver<S, T> suggestionResolver) {
        suggestionResolverRegistry.registerResolverForType(suggestionResolver);
    }
    
    /**
     * Registers a suggestion resolver to a type
     *
     * @param type               the type
     * @param suggestionResolver the suggestion resolver.
     */
    @Override
    public void registerSuggestionResolver(Type type, SuggestionResolver<S> suggestionResolver) {
        suggestionResolverRegistry.registerResolverForType(type, suggestionResolver);
    }
    
    /**
     * Fetches the suggestion provider/resolver for a specific argument
     *
     * @param name the name of the argument
     * @return the {@link SuggestionResolver} instance for that argument
     */
    public @Nullable SuggestionResolver<S> getNamedSuggestionResolver(String name) {
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
    public void registerNamedSuggestionResolver(String name, SuggestionResolver<S> suggestionResolver) {
        suggestionResolverRegistry.registerNamedResolver(name, suggestionResolver);
    }
    
    /**
     * Registers a placeholder
     *
     * @param placeholder to register
     */
    @Override
    public void registerPlaceholder(Placeholder<S> placeholder) {
        placeholderRegistry.setData(placeholder.id(), placeholder);
    }
    
    /**
     * The id/format of this placeholder, must be unique and lowercase
     *
     * @param id the id for the placeholder
     * @return the placeholder
     */
    @Override
    public Optional<Placeholder<S>> getPlaceHolder(String id) {
        return placeholderRegistry.getData(id);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public @Nullable <R> SourceResolver<S, R> getSourceResolver(Type type) {
        return (SourceResolver<S, R>) sourceResolverRegistry.getData(type).orElse(null);
    }
    
    @Override
    public <R> void registerSourceResolver(Type type, SourceResolver<S, R> sourceResolver) {
        sourceResolverRegistry.setData(type, sourceResolver);
    }

    /**
     * Sets the usage verifier to a new instance
     *
     * @param usageVerifier the usage verifier to set
     */
    @Override
    public void setUsageVerifier(UsageVerifier<S> usageVerifier) {
        this.verifier = usageVerifier;
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
    
    /**
     * Registers a command pre-processor
     *
     * @param preProcessor the pre-processor to register
     */
    @Override
    public void registerGlobalPreProcessor(CommandPreProcessor<S> preProcessor) {
        Preconditions.notNull(preProcessor, "Pre-processor");
        globalPreProcessors.add(preProcessor);
    }
    
    /**
     * Registers a command post-processor
     *
     * @param postProcessor the post-processor to register
     */
    @Override
    public void registerGlobalPostProcessor(CommandPostProcessor<S> postProcessor) {
        Preconditions.notNull(postProcessor, "Post-processor");
        globalPostProcessors.add(postProcessor);
    }
    
    /**
     * Registers a command pre-processor
     *
     * @param priority     the priority for the processor
     * @param preProcessor the pre-processor to register
     */
    @Override
    public void registerGlobalPreProcessor(int priority, CommandPreProcessor<S> preProcessor) {
        Preconditions.notNull(preProcessor, "Pre-processor");
        globalPreProcessors.add(priority, preProcessor);
    }
    
    /**
     * Registers a command post-processor
     *
     * @param priority      the priority for the processor
     * @param postProcessor the post-processor to register
     */
    @Override
    public void registerGlobalPostProcessor(int priority, CommandPostProcessor<S> postProcessor) {
        Preconditions.notNull(postProcessor, "Post-processor");
        globalPostProcessors.add(priority, postProcessor);
    }
    
    
    @Override
    public @NotNull UsageMatchResult dispatch(Context<S> context) {
        try {
            return handleExecution(context);
        } catch (Throwable ex) {
            this.handleThrowable(ex, context, BaseImperat.class, "dispatch");
            return UsageMatchResult.UNKNOWN;
        }
    }
    
    @Override
    public @NotNull UsageMatchResult dispatch(S source, Command<S> command, String[] rawInput) {
        ArgumentQueue rawArguments = ArgumentQueue.parse(rawInput);
        
        Context<S> plainContext = getContextFactory()
                .createContext(this, source, command, rawArguments);
        
        return dispatch(plainContext);
    }
    
    @Override
    public @NotNull UsageMatchResult dispatch(S source, String commandName, String[] rawInput) {
        Command<S> command = getCommand(commandName);
        if (command == null) {
            //TODO better message here
            source.reply("Unknown command !");
            return UsageMatchResult.UNKNOWN;
        }
        //temp debugging
        //command.visualize();
        return dispatch(source, command, rawInput);
    }
    
    @Override
    public @NotNull UsageMatchResult dispatch(S sender, String commandName, String rawArgsOneLine) {
        return dispatch(sender, commandName, rawArgsOneLine.split(" "));
    }
    
    @Override
    public UsageMatchResult dispatch(S sender, String line) {
        String[] lineArgs = line.split(" ");
        String[] argumentsOnly = new String[lineArgs.length - 1];
        System.arraycopy(lineArgs, 1, argumentsOnly, 0, lineArgs.length - 1);
        return dispatch(sender, lineArgs[0], argumentsOnly);
    }
    
    private UsageMatchResult handleExecution(Context<S> context) throws ImperatException {
        Command<S> command = context.command();
        S source = context.source();
        
        if (!getPermissionResolver().hasPermission(source, command.permission())) {
            throw new PermissionDeniedException();
        }
        
        if (context.arguments().isEmpty()) {
            CommandUsage<S> defaultUsage = command.getDefaultUsage();
            executeUsage(command, source, context, defaultUsage);
            return UsageMatchResult.INCOMPLETE;
        }
        
        CommandDispatch<S> searchResult = command.contextMatch(context);
        //executing usage
        
        CommandUsage<S> usage = searchResult.toUsage(command);
        
        if (searchResult.result() == UsageMatchResult.COMPLETE)
            executeUsage(command, source, context, usage);
        else if (searchResult.result() == UsageMatchResult.INCOMPLETE) {
            var lastParameter = searchResult.getLastParameter();
            if (lastParameter.isCommand()) {
                executeUsage(command, source, context, lastParameter.asCommand().getDefaultUsage());
            } else {
                if (usage != null)
                    executeUsage(command, source, context, usage);
                else
                    throw new IllegalStateException("Matched Usage is null while the result is INCOMPLETE, this error is internal and almost impossible to occur, please contact mqzen or iiahmedyt on discord");
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
        
        ResolvedContext<S> resolvedContext = contextFactory.createResolvedContext(this, context, usage);
        resolvedContext.resolve();
        
        //global post-processing
        postProcess(resolvedContext);
        
        //per command post-processing
        command.postProcess(this, resolvedContext, usage);
        
        //executing the usage
        usage.execute(this, source, resolvedContext);
    }
    
    //TODO improve (DRY)
    private void preProcess(
            @NotNull Context<S> context,
            @NotNull CommandUsage<S> usage
    ) {
        
        for (CommandPreProcessor<S> preProcessor : globalPreProcessors) {
            try {
                preProcessor.process(this, context, usage);
            } catch (Throwable ex) {
                this.handleThrowable(
                        ex,
                        context, preProcessor.getClass(),
                        "CommandPreProcessor#process"
                );
                break;
            }
        }
    }
    
    //TODO improve (DRY)
    private void postProcess(
            @NotNull ResolvedContext<S> context
    ) {
        for (CommandPostProcessor<S> postProcessor : globalPostProcessors) {
            try {
                postProcessor.process(this, context);
            } catch (Throwable ex) {
                this.handleThrowable(
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
    public Collection<String> autoComplete(Command<S> command, S source, String[] args) {
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
    
    /**
     * @return The template for showing help
     */
    @Override
    public @Nullable HelpProvider<S> getHelpProvider() {
        return provider;
    }
    
    /**
     * Set the help template to use
     *
     * @param template the help template
     */
    @Override
    public void setHelpProvider(@Nullable HelpProvider<S> template) {
        this.provider = template;
    }
    
    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends Throwable> ThrowableResolver<T, S> getThrowableResolver(Class<T> exception) {
        Class<?> current = exception;
        while (current != null && Throwable.class.isAssignableFrom(current)) {
            if (handlers.containsKey(current)) {
                return (ThrowableResolver<T, S>) handlers.get(current);
            }
            current = current.getSuperclass();
        }
        return null;
    }
    
    @Override
    public <T extends Throwable> void setThrowableResolver(Class<T> exception, ThrowableResolver<T, S> handler) {
        this.handlers.put(exception, handler);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void handleThrowable(
            @NotNull final Throwable throwable,
            final Context<S> context,
            final Class<?> owning,
            final String methodName
    ) {
        Throwable current = throwable;
        
        while (current != null) {
            if (current instanceof SelfHandledException selfHandledException) {
                selfHandledException.handle(this, context);
                return;
            }
            
            ThrowableResolver<? super Throwable, S> handler = (ThrowableResolver<? super Throwable, S>) this.getThrowableResolver(current.getClass());
            if (handler != null) {
                handler.resolve(current, this, context);
                return;
            }
            
            current = current.getCause();
        }
        
        ImperatDebugger.error(owning, methodName, throwable);
    }
    
    @Override
    public void debug(boolean treeVisualizing) {
        for (var cmd : commands.values()) {
            if (treeVisualizing) {
                cmd.visualizeTree();
            } else {
                ImperatDebugger.debug("Debugging command '%s'", cmd.name());
                for (CommandUsage<S> usage : cmd.usages()) {
                    ImperatDebugger.debug("   - '%s'", usage);
                }
            }
        }
    }
    
}
