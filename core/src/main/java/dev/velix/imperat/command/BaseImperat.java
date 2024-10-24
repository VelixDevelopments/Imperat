package dev.velix.imperat.command;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.base.AnnotationParser;
import dev.velix.imperat.annotations.base.AnnotationReader;
import dev.velix.imperat.annotations.base.AnnotationReplacer;
import dev.velix.imperat.annotations.base.element.ParameterElement;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.type.ParameterType;
import dev.velix.imperat.command.processors.CommandPostProcessor;
import dev.velix.imperat.command.processors.CommandPreProcessor;
import dev.velix.imperat.command.processors.CommandProcessor;
import dev.velix.imperat.command.processors.impl.UsageCooldownProcessor;
import dev.velix.imperat.command.processors.impl.UsagePermissionProcessor;
import dev.velix.imperat.command.suggestions.SuggestionResolverRegistry;
import dev.velix.imperat.command.tree.CommandDispatch;
import dev.velix.imperat.context.*;
import dev.velix.imperat.context.internal.ContextFactory;
import dev.velix.imperat.exception.*;
import dev.velix.imperat.help.HelpProvider;
import dev.velix.imperat.placeholders.Placeholder;
import dev.velix.imperat.placeholders.PlaceholderRegistry;
import dev.velix.imperat.placeholders.PlaceholderResolver;
import dev.velix.imperat.resolvers.*;
import dev.velix.imperat.util.ImperatDebugger;
import dev.velix.imperat.util.Preconditions;
import dev.velix.imperat.util.Registry;
import dev.velix.imperat.util.TypeWrap;
import dev.velix.imperat.verification.UsageVerifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public abstract class BaseImperat<S extends Source> implements Imperat<S> {

    private final ContextResolverRegistry<S> contextResolverRegistry;
    private final ParamTypeRegistry<S> paramTypeRegistry;
    private final SuggestionResolverRegistry<S> suggestionResolverRegistry;
    private final PlaceholderRegistry<S> placeholderRegistry;
    private final SourceResolverRegistry<S> sourceResolverRegistry;

    protected @NotNull PermissionResolver<S> permissionResolver;
    private @NotNull ContextFactory<S> contextFactory;
    private @NotNull UsageVerifier<S> verifier;
    private @Nullable HelpProvider<S> provider = null;
    private @NotNull AnnotationParser<S> annotationParser;

    private final Registry<Type, DependencySupplier> dependencyResolverRegistry = new Registry<>();

    private final Map<String, Command<S>> commands = new HashMap<>();
    private final Map<Class<? extends Throwable>, ThrowableResolver<?, S>> handlers = new HashMap<>();

    private final Queue<CommandPreProcessor<S>> globalPreProcessors;
    private final Queue<CommandPostProcessor<S>> globalPostProcessors;

    protected BaseImperat(@NotNull PermissionResolver<S> permissionResolver) {
        final Comparator<CommandProcessor> commandProcessorComparator = Comparator.comparingDouble(CommandProcessor::priority);
        globalPreProcessors = new PriorityQueue<>(commandProcessorComparator);
        globalPostProcessors = new PriorityQueue<>(commandProcessorComparator);

        contextFactory = ContextFactory.defaultFactory(this);
        contextResolverRegistry = ContextResolverRegistry.createDefault(this);
        paramTypeRegistry = ParamTypeRegistry.createDefault();
        suggestionResolverRegistry = SuggestionResolverRegistry.createDefault(this);
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
            throw new UnsupportedOperationException("Couldn't find any source resolver for valueType `"
                + exception.getTargetType().getTypeName() + "'");
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
     * Registers the dependency to the type
     *
     * @param type     the type for the dependency
     * @param resolver the resolver
     */
    @Override
    public void registerDependencyResolver(Type type, DependencySupplier resolver) {
        this.dependencyResolverRegistry.setData(type, resolver);
    }

    /**
     * Resolves dependency of certain type
     *
     * @param type the type
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable T resolveDependency(Type type) {
        return (T) dependencyResolverRegistry.getData(type)
            .map(DependencySupplier::get).orElse(null);
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
     * Checks whether the valueType has
     * a registered context-resolver
     *
     * @param type the valueType
     * @return whether the valueType has
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
    public void registerContextResolverFactory(Type type, ContextResolverFactory<S> factory) {
        contextResolverRegistry.registerFactory(type, factory);
    }

    /**
     * @return returns the factory for creation of
     * {@link ContextResolver}
     */
    @Override
    public @Nullable ContextResolverFactory<S> getContextResolverFactory(Type type) {
        return contextResolverRegistry.getFactoryFor(type).orElse(null);
    }

    /**
     * Fetches {@link ContextResolver} for a certain valueType
     *
     * @param resolvingContextType the valueType for this resolver
     * @return the context resolver
     */
    @Override
    public <T> @Nullable ContextResolver<S, T> getContextResolver(Type resolvingContextType) {
        return contextResolverRegistry.getResolverWithoutParameterElement(resolvingContextType);
    }

    /**
     * Fetches the context resolver for {@link ParameterElement} of a method
     *
     * @param element the element
     * @return the {@link ContextResolver} for this element
     */
    @Override
    public <T> @Nullable ContextResolver<S, T> getMethodParamContextResolver(@NotNull ParameterElement element) {
        Preconditions.notNull(element, "element");
        return contextResolverRegistry.getContextResolver(element.getType(), element);
    }

    /**
     * Registers {@link ContextResolver}
     *
     * @param type     the class-valueType of value being resolved from context
     * @param resolver the resolver for this value
     */
    @Override
    public <T> void registerContextResolver(Type type,
                                            @NotNull ContextResolver<S, T> resolver) {
        contextResolverRegistry.registerResolver(type, resolver);
    }

    /**
     * Registers {@link dev.velix.imperat.command.parameters.type.ParameterType}
     *
     * @param type     the class-valueType of value being resolved from context
     * @param resolver the resolver for this value
     */
    @Override
    public <T> void registerParamType(Type type, @NotNull ParameterType<S, T> resolver) {
        paramTypeRegistry.registerResolver(type, resolver);
    }


    /**
     * Fetches {@link ParameterType} for a certain value
     *
     * @param resolvingValueType the value that the resolver ends providing it from the context
     * @return the context resolver of a certain valueType
     */
    @Override
    public @Nullable ParameterType<S, ?> getParameterType(Type resolvingValueType) {
        return paramTypeRegistry.getResolver(resolvingValueType).orElse(null);
    }

    /**
     * Fetches the suggestion provider/resolver for a specific valueType of
     * argument or parameter.
     *
     * @param type the valueType
     * @return the {@link SuggestionResolver} instance for that valueType
     */
    @Override
    public @Nullable SuggestionResolver<S> getSuggestionResolverByType(Type type) {
        return paramTypeRegistry.getResolver(type)
            .map(ParameterType::getSuggestionResolver)
            .orElse(null);
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

    /**
     * Replaces the placeholders of input by their {@link PlaceholderResolver}
     *
     * @param input the input
     * @return the processed/replaced text input.
     */
    @Override
    public @NotNull String replacePlaceholders(String input) {
        return placeholderRegistry.resolvedString(input);
    }

    /**
     * Replaces the placeholders on each string of the array,
     * modifying the input array content.
     *
     * @param array the array to replace its string contents
     * @return The placeholder replaced String array
     */
    @Override
    public @NotNull String[] replacePlaceholders(String[] array) {
        return placeholderRegistry.resolvedArray(array);
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

    @Override
    public @NotNull CommandDispatch.Result dispatch(Context<S> context) {
        try {
            return handleExecution(context);
        } catch (Throwable ex) {
            this.handleThrowable(ex, context, BaseImperat.class, "dispatch");
            return CommandDispatch.Result.UNKNOWN;
        }
    }

    @Override
    public @NotNull CommandDispatch.Result dispatch(S source, Command<S> command, String[] rawInput) {
        ArgumentQueue rawArguments = ArgumentQueue.parse(rawInput);

        Context<S> plainContext = getContextFactory()
            .createContext(source, command, rawArguments);

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

        if (!getPermissionResolver().hasPermission(source, command.permission())) {
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
        if (usage == null) {
            throw new InvalidSyntaxException();
        }

        //executing usage
        if (searchResult.result() == CommandDispatch.Result.COMPLETE)
            executeUsage(command, source, context, usage);
        else if (searchResult.result() == CommandDispatch.Result.INCOMPLETE) {
            var lastParameter = searchResult.getLastParameter();
            if (lastParameter.isCommand()) {
                executeUsage(command, source, context, lastParameter.asCommand().getDefaultUsage());
            } else {
                executeUsage(command, source, context, usage);
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

        ResolvedContext<S> resolvedContext = contextFactory.createResolvedContext(context, usage);
        resolvedContext.resolve();

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
    public CompletableFuture<Collection<String>> autoComplete(Command<S> command, S source, String[] args) {
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
            var resolver = handlers.get(current);
            if (resolver != null) {
                return (ThrowableResolver<T, S>) resolver;
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
                    ImperatDebugger.debug("   - '%s'", CommandUsage.format(cmd, usage));
                }
            }
        }
    }

}
