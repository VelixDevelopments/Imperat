package dev.velix.imperat.command;

import dev.velix.imperat.CommandDebugger;
import dev.velix.imperat.CommandDispatcher;
import dev.velix.imperat.CommandSource;
import dev.velix.imperat.annotations.AnnotationParser;
import dev.velix.imperat.annotations.AnnotationReplacer;
import dev.velix.imperat.caption.Caption;
import dev.velix.imperat.caption.CaptionKey;
import dev.velix.imperat.caption.CaptionRegistry;
import dev.velix.imperat.caption.Messages;
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
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiStatus.Internal
public abstract class BaseCommandDispatcher<C> implements CommandDispatcher<C> {
	
	public final static Component START_PREFIX = Messages.getMsg("<dark_gray><bold>[<gold>!</gold>]</bold></dark_gray> ");
	public final static Component CAPTION_EXECUTION_ERROR_PREFIX = START_PREFIX.append(Messages.getMsg("<red><bold>Execution error:</bold></red> "));
	public final static Component FULL_SYNTAX_PREFIX = START_PREFIX.append(Messages.getMsg("<dark_aqua>Full syntax:</dark_aqua> "));
	
	public final static HelpTemplate DEFAULT_HELP_TEMPLATE = new DefaultTemplate();
	
	private final Map<String, Command<C>> commands = new HashMap<>();
	
	private ContextFactory<C> contextFactory;
	
	private final ContextResolverRegistry<C> contextResolverRegistry;
	
	private final ValueResolverRegistry<C> valueResolverRegistry;
	
	private final SuggestionResolverRegistry<C> suggestionResolverRegistry;
	
	private @NotNull UsageVerifier<C> verifier;
	
	private final CaptionRegistry<C> captionRegistry;
	
	private HelpTemplate template = DEFAULT_HELP_TEMPLATE;
	
	private AnnotationParser<C> annotationParser;
	
	protected PermissionResolver<C> permissionResolver;
	
	protected BaseCommandDispatcher(PermissionResolver<C> permissionResolver) {
		contextFactory = ContextFactory.defaultFactory();
		contextResolverRegistry = ContextResolverRegistry.createDefault();
		valueResolverRegistry = ValueResolverRegistry.createDefault();
		suggestionResolverRegistry = SuggestionResolverRegistry.createDefault();
		verifier = UsageVerifier.defaultVerifier();
		captionRegistry = CaptionRegistry.createDefault();
		annotationParser = AnnotationParser.defaultParser(this);
		this.permissionResolver = permissionResolver;
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
			CommandDebugger.error(BaseCommandDispatcher.class, "registerCommand(Command command)", ex);
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
	public <T> void registerCommand(T command) {
		annotationParser.parseCommandClass(command);
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
	public boolean hasContextResolver(Class<?> type) {
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
	public <T> @Nullable ContextResolver<C, T> getContextResolver(Class<T> resolvingContextType) {
		return contextResolverRegistry.getResolver(resolvingContextType);
	}
	
	/**
	 * Registers {@link ContextResolver}
	 *
	 * @param type     the class-type of value being resolved from context
	 * @param resolver the resolver for this value
	 */
	@Override
	public <T> void registerContextResolver(Class<T> type,
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
	public <T> void registerValueResolver(Class<T> type, @NotNull ValueResolver<C, T> resolver) {
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
	public @Nullable <T> ValueResolver<C, T> getValueResolver(Class<T> resolvingValueType) {
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
	public @Nullable <T> SuggestionResolver<C, T> getNamedSuggestionResolver(Class<T> clazz) {
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
		return CommandSource.class.isAssignableFrom(type);
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
	@Override
	public @Nullable <T> SuggestionResolver<C, T> getNamedSuggestionResolver(String name) {
		return suggestionResolverRegistry.getResolverByName(name);
	}
	
	/**
	 * Registers a suggestion resolver linked
	 * directly to the argument only , not the general
	 * type of this argument
	 *
	 * @param argumentName       the argument name
	 * @param suggestionResolver the suggestion resolver to register
	 */
	@Override
	public <T> void registerNamedSuggestionResolver(String argumentName, SuggestionResolver<C, T> suggestionResolver) {
		suggestionResolverRegistry.registerNamedResolver(argumentName, suggestionResolver);
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
	 * Registers a caption
	 *
	 * @param caption the caption to register
	 */
	@Override
	public void registerCaption(Caption<C> caption) {
		this.captionRegistry.registerCaption(caption);
	}
	
	/**
	 * Sends a caption to the source
	 *
	 * @param command the command
	 * @param key     the id/key of the caption
	 * @param source  the command sender
	 * @param context the context of the command
	 * @param usage   the usage of the command
	 */
	@Override
	public void sendCaption(CaptionKey key,
	                        @NotNull Command<C> command,
	                        CommandSource<C> source,
	                        Context<C> context,
	                        @Nullable CommandUsage<C> usage) {
		this.sendCaption(key, command, source, context, usage, null);
	}
	
	/**
	 * Sends a caption to the source
	 *
	 * @param key       the id/key of the caption
	 * @param command   the command
	 * @param source    the command sender
	 * @param context   the context of the command
	 * @param usage     the usage of the command, null if not resolved yet
	 * @param exception the error if present
	 */
	@Override
	public void sendCaption(CaptionKey key,
	                        @NotNull Command<C> command,
	                        CommandSource<C> source,
	                        Context<C> context,
	                        @Nullable CommandUsage<C> usage,
	                        @Nullable Exception exception) {
		Caption<C> caption = captionRegistry.getCaption(key);
		if (caption == null) {
			throw new IllegalStateException(String.format("Unregistered caption from key '%s'", key.id()));
		}
		source.reply(CAPTION_EXECUTION_ERROR_PREFIX.append(caption.asComponent(this, command, source, context, usage)));
	}
	
	/**
	 * Sends a {@link Caption} that requires dynamic input
	 * through it's constructor
	 *
	 * @param source  the command source
	 * @param context the context of the command
	 * @param caption the caption to send
	 */
	@Override
	public void sendDynamicCaption(CommandSource<C> source,
	                               Context<C> context,
	                               Caption<C> caption) {
		ResolvedContext<C> resolvedContext = (ResolvedContext<C>) context;
		source.reply(CAPTION_EXECUTION_ERROR_PREFIX.append(
						caption.asComponent(this, resolvedContext.getOwningCommand(), source,
										context, resolvedContext.getDetectedUsage())
		));
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
		CommandSource<C> commandSource = wrapSender(sender);
		
		Context<C> plainContext = getContextFactory()
						.createContext(this, commandSource, commandName, rawArguments);
		
		try {
			handleExecution(commandSource, plainContext);
		} catch (Throwable ex) {
			
			Throwable current = ex;
			if(current instanceof CommandException commandException) {
				commandException.handle(plainContext);
			}
			else {
				while (current != null && !(current instanceof CommandException)) {
					current = current.getCause();
				}
				if(current != null) {
					((CommandException)current).handle(plainContext);
				}else {
					CommandDebugger.error(BaseCommandDispatcher.class, "dispatch", ex);
				}
			}
			
		}
		
	}
	
	
	private void handleExecution(CommandSource<C> source, Context<C> context) throws CommandException {
		
		Command<C> command = getCommand(context.getCommandUsed());
		if (command == null) {
			source.reply("Unknown command !");
			return;
		}
		
		if (!getPermissionResolver().hasPermission(source, command.getPermission())) {
			sendCaption(CaptionKey.NO_PERMISSION, command, source, context, null);
			return;
		}
		
		//context.extractCommandFlags(command);
		
		CommandUsageLookup<C>.SearchResult searchResult = command.lookup(this)
						.searchUsage(context);
		
		if ((searchResult.getCommandUsage() == null
						&& searchResult.getResult() == CommandUsageLookup.Result.FOUND_INCOMPLETE)
						|| context.getArguments().isEmpty()) {
			CommandUsage<C> defaultUsage = command.getDefaultUsage();
			defaultUsage.execute(source, context);
			return;
		}
		
		//executing usage
		CommandUsage<C> usage = searchResult.getCommandUsage();
		if (!getPermissionResolver().hasUsagePermission(source, usage)) {
			sendCaption(CaptionKey.NO_PERMISSION, command, source, context, null);
			return;
		}
		
		if (searchResult.getResult() == CommandUsageLookup.Result.FOUND_COMPLETE)
			executeUsage(command, source, context, usage);
		else
			sendCaption(CaptionKey.INVALID_SYNTAX, command, source, context, usage);
		
	}
	
	private void executeUsage(Command<C> command,
	                          CommandSource<C> source,
	                          Context<C> context,
	                          final CommandUsage<C> usage) throws CommandException {
		if (usage.getCooldownHandler().hasCooldown(source)) {
			sendCaption(CaptionKey.COOLDOWN, command, source, context, usage);
			return;
		}
		usage.getCooldownHandler().registerExecutionMoment(source);
		ResolvedContext<C> resolvedContext = contextFactory.createResolvedContext(this, command, context, usage);
		resolvedContext.resolve();
		usage.execute(source, resolvedContext);
	}
	
	/**
	 * @param command the data about the command being written in the chat box
	 * @param sender  the sender writing the command
	 * @param args    the arguments currently written
	 * @return the suggestions at the current position
	 */
	@Override
	public List<String> suggest(Command<C> command, C sender, String[] args) {
		CommandSource<C> source = wrapSender(sender);
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
