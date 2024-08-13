package dev.velix.imperat;


import dev.velix.imperat.annotations.AnnotationReplacer;
import dev.velix.imperat.caption.Caption;
import dev.velix.imperat.caption.CaptionKey;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.UsageParameter;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.ContextFactory;
import dev.velix.imperat.context.internal.ContextResolverFactory;
import dev.velix.imperat.help.HelpTemplate;
import dev.velix.imperat.resolvers.ContextResolver;
import dev.velix.imperat.resolvers.PermissionResolver;
import dev.velix.imperat.resolvers.SuggestionResolver;
import dev.velix.imperat.resolvers.ValueResolver;
import dev.velix.imperat.verification.UsageVerifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

/**
 * Represents the class that handles all
 * commands' registrations and executions
 * It also caches the settings that the user can
 * change or modify in the api.
 *
 * @param <C> the command sender type
 */
@ApiStatus.AvailableSince("1.0.0")
public interface CommandDispatcher<C> {


	/**
	 * @return The command prefix
	 */
	String commandPrefix();

	/**
	 * Registering a command into the dispatcher
	 *
	 * @param command the command to register
	 */
	void registerCommand(Command<C> command);

	/**
	 * Registers a command class built by the
	 * annotations using a parser
	 *
	 * @param command the annotated command instance to parse
	 * @param <T>     the type of this class instance
	 */
	<T> void registerCommand(T command);

	/**
	 * Registers annotation replacer
	 * @param type the type to replace the annotation by
	 * @param replacer the replacer
	 * @param <A> the type of annotation to replace
	 */
	<A extends Annotation> void registerAnnotationReplacer(Class<A> type,
	                                                       AnnotationReplacer<A> replacer);

	/**
	 * @param name the name/alias of the command
	 * @return fetches {@link Command} with specific name
	 */
	@Nullable
	Command<C> getCommand(String name);

	/**
	 * @param parameter the parameter
	 * @return the command from the parameter's name
	 */
	default @Nullable Command<C> getCommand(UsageParameter parameter) {
		return getCommand(parameter.getName());
	}

	/**
	 * @param owningCommand the command owning this sub-command
	 * @param name          the name of the subcommand you're looking for
	 * @return the subcommand of a command
	 */
	@Nullable
	Command<C> getSubCommand(String owningCommand, String name);

	/**
	 * @return the factory for creation of
	 * command related contexts {@link Context}
	 */
	ContextFactory<C> getContextFactory();

	/**
	 * sets the context factory {@link ContextFactory} for the contexts
	 *
	 * @param contextFactory the context factory to set
	 */
	void setContextFactory(ContextFactory<C> contextFactory);


	/**
	 * @return {@link PermissionResolver} for the dispatcher
	 */
	PermissionResolver<C> getPermissionResolver();


	/**
	 * Registers a context resolver factory
	 * @param factory the factory to register
	 */
	void registerContextResolverFactory(ContextResolverFactory<C> factory);

	/**
	 * Checks whether the type has
	 * a registered context-resolver
	 *
	 * @param type the type
	 * @return whether the type has
	 * a context-resolver
	 */
	boolean hasContextResolver(Class<?> type);

	/**
	 * @return returns the factory for creation of
	 * {@link ContextResolver}
	 */
	ContextResolverFactory<C> getContextResolverFactory();

	/**
	 * Fetches {@link ContextResolver} for a certain type
	 * @param resolvingContextType the type for this resolver
	 *
	 * @return the context resolver
	 * @param <T> the type of class
	 */
	<T> ContextResolver<C, T> getContextResolver(Class<T> resolvingContextType);

	/**
	 * Fetches the {@link ContextResolver} suitable for the {@link UsageParameter}
	 *
	 * @param usageParameter the parameter of a command's usage
	 * @param <T>  the type of value that will be resolved by {@link ValueResolver}
	 * @return the context resolver for this parameter's value type
	 */
	@SuppressWarnings("unchecked")
	default <T> ContextResolver<C, T> getContextResolver(UsageParameter usageParameter) {
		return (ContextResolver<C, T>) getContextResolver(usageParameter.getType());
	}

	/**
	 * Registers {@link ContextResolver}
	 *
	 * @param type     the class-type of value being resolved from context
	 * @param resolver the resolver for this value
	 * @param <T>      the type of value being resolved from context
	 */
		  <T>
	void registerContextResolver(Class<T> type, @NotNull ContextResolver<C, T> resolver);

	/**
	 * Fetches {@link ValueResolver} for a certain value
	 *
	 * @param resolvingValueType the value that the resolver ends providing it from the context
	 * @param <T>                the type of value resolved from the context resolver
	 * @return the value resolver of a certain type
	 */
	@Nullable
	<T> ValueResolver<C, T> getValueResolver(Class<T> resolvingValueType);

	/**
	 * Fetches the {@link ValueResolver} suitable for the {@link UsageParameter}
	 *
	 * @param usageParameter the parameter of a command's usage
	 * @param <T>            the type of value that will be resolved by {@link ValueResolver}
	 * @return the value resolver for this parameter's value type
	 */
	@SuppressWarnings("unchecked")
	default <T> ValueResolver<C, T> getValueResolver(UsageParameter usageParameter) {
		return (ValueResolver<C, T>) getValueResolver(usageParameter.getType());
	}

	/**
	 * Registers {@link ValueResolver}
	 *
	 * @param type     the class-type of value being resolved from context
	 * @param resolver the resolver for this value
	 * @param <T>      the type of value being resolved from context
	 */
	<T> void registerValueResolver(Class<T> type, @NotNull ValueResolver<C, T> resolver);

	/**
	 * @return all currently registered {@link ValueResolver}
	 */
	Collection<? extends ValueResolver<C, ?>> getRegisteredValueResolvers();

	/**
	 * Fetches the suggestion provider/resolver for a specific type of
	 * argument or parameter.
	 *
	 * @param parameter the parameter symbolizing the type and argument name
	 * @param <T>       the type parameter representing the type of value that the suggestion resolver
	 *                  will work with
	 * @return the {@link SuggestionResolver} instance for that type
	 */
	@SuppressWarnings("unchecked")
	default @Nullable <T> SuggestionResolver<C, T> getSuggestionResolver(UsageParameter parameter) {
		SuggestionResolver<C, T> resolver = getSuggestionResolver((Class<T>) parameter.getType());
		SuggestionResolver<C, T> argResolver = getArgumentSuggestionResolver(parameter.getName());

		if (argResolver != null) {
			return argResolver;
		}

		return resolver;
	}

	/**
	 * Fetches the suggestion provider/resolver for a specific type of
	 * argument or parameter.
	 *
	 * @param clazz the clazz symbolizing the type
	 * @param <T>   the type parameter representing the type of value that the suggestion resolver
	 *              will work with
	 * @return the {@link SuggestionResolver} instance for that type
	 */
	@Nullable
	<T> SuggestionResolver<C, T> getSuggestionResolver(Class<T> clazz);

	/**
	 * Fetches the suggestion provider/resolver for a specific argument
	 *
	 * @param name the name of the argument
	 * @param <T>  the type parameter representing the type of value that the suggestion resolver
	 *             will work with
	 * @return the {@link SuggestionResolver} instance for that argument
	 */
	@Nullable
	<T> SuggestionResolver<C, T> getArgumentSuggestionResolver(String name);

	/**
	 * Registers a suggestion resolver
	 *
	 * @param suggestionResolver the suggestion resolver to register
	 * @param <T>                the type of value that the suggestion resolver will work with.
	 */
	<T> void registerSuggestionResolver(SuggestionResolver<C, T> suggestionResolver);

	/**
	 * Registers a suggestion resolver
	 *
	 * @param suggestionResolver the suggestion resolver to register
	 * @param <T>                the type of value that the suggestion resolver will work with.
	 */
	<T> void registerArgumentSuggestionResolver(String argumentName, SuggestionResolver<C, T> suggestionResolver);

	/**
	 * Sets the usage verifier to a new instance
	 *
	 * @param usageVerifier the usage verifier to set
	 */
	void setUsageVerifier(UsageVerifier<C> usageVerifier);

	/**
	 * Registers a caption
	 *
	 * @param caption the caption to register
	 */
	void registerCaption(Caption<C> caption);


	/**
	 * Sends a caption to the source
	 *
	 * @param source    the command sender
	 * @param context   the context of the command
	 * @param usage     the usage of the command, null if not resolved yet
	 * @param exception the error
	 * @param key       the id/key of the caption
	 */
	void sendCaption(CaptionKey key,
	                 @NotNull Command<C> command,
	                 CommandSource<C> source,
	                 Context<C> context,
	                 @Nullable CommandUsage<C> usage,
	                 @Nullable Exception exception
	);


	/**
	 * Sends a caption to the source
	 *
	 * @param key     the id/key of the caption
	 * @param command the command detected
	 * @param source  the command sender
	 * @param context the context of the command
	 */
	void sendCaption(CaptionKey key,
	                 @NotNull Command<C> command,
	                 CommandSource<C> source,
	                 Context<C> context,
	                 @Nullable CommandUsage<C> usage
	);

	/**
	 * Wraps the sender into a built-in command-sender type
	 *
	 * @param sender the sender's actual value
	 * @return the wrapped command-sender type
	 */
	CommandSource<C> wrapSender(C sender);

	/**
	 * Checks whether the type can be a command sender
	 *
	 * @param type the type
	 * @return whether the type can be a command sender
	 */
	boolean canBeSender(Class<?> type);

	/**
	 * Dispatches and executes a command with certain raw arguments
	 *
	 * @param sender      the sender/executor of this command
	 * @param commandName the name of the command to execute
	 * @param rawInput    the command's args input
	 */
	void dispatch(C sender, String commandName, String... rawInput);


	/**
	 * @return the platform of the module
	 */
	Object getPlatform();

	/**
	 * @param command the data about the command being written in the chat box
	 * @param sender  the sender writing the command
	 * @param args    the arguments currently written
	 * @return the suggestions at the current position
	 */
	List<String> suggest(Command<C> command, C sender, String[] args);

	/**
	 * Gets all registered commands
	 *
	 * @return the registered commands
	 */
	Collection<? extends Command<C>> getRegisteredCommands();

	/**
	 * @return The template for showing help
	 */
	@NotNull HelpTemplate getHelpTemplate();

	/**
	 * Set the help template to use
	 * @param template the help template
	 */
	void setHelpTemplate(HelpTemplate template);
}
