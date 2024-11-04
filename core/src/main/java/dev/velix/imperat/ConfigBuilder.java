package dev.velix.imperat;

import dev.velix.imperat.command.ContextResolverFactory;
import dev.velix.imperat.command.parameters.type.ParameterType;
import dev.velix.imperat.command.processors.CommandPostProcessor;
import dev.velix.imperat.command.processors.CommandPreProcessor;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.ContextFactory;
import dev.velix.imperat.exception.ThrowableResolver;
import dev.velix.imperat.help.HelpProvider;
import dev.velix.imperat.placeholders.Placeholder;
import dev.velix.imperat.resolvers.*;
import dev.velix.imperat.verification.UsageVerifier;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

public abstract class ConfigBuilder<S extends Source, I extends Imperat<S>> {

    protected final ImperatConfig<S> config;

    ConfigBuilder() {
        config = new ImperatConfigImpl<>();
    }


    // Command Prefix
    public ConfigBuilder<S, I> commandPrefix(String cmdPrefix) {
        config.setCommandPrefix(cmdPrefix);
        return this;
    }

    // Permission Resolver
    public ConfigBuilder<S, I> permissionResolver(PermissionResolver<S> permissionResolver) {
        config.setPermissionResolver(permissionResolver);
        return this;
    }

    // Context Factory
    public ConfigBuilder<S, I> contextFactory(ContextFactory<S> contextFactory) {
        config.setContextFactory(contextFactory);
        return this;
    }

    // Usage Verifier
    public ConfigBuilder<S, I> usageVerifier(UsageVerifier<S> usageVerifier) {
        config.setUsageVerifier(usageVerifier);
        return this;
    }

    // Dependency Resolver
    public ConfigBuilder<S, I> dependencyResolver(Type type, DependencySupplier resolver) {
        config.registerDependencyResolver(type, resolver);
        return this;
    }

    // Throwable Resolver
    public <T extends Throwable> ConfigBuilder<S, I> throwableResolver(
        Class<T> exception, ThrowableResolver<T, S> handler) {
        config.setThrowableResolver(exception, handler);
        return this;
    }

    // Command Pre-Processor
    public ConfigBuilder<S, I> preProcessor(CommandPreProcessor<S> preProcessor) {
        config.registerGlobalPreProcessor(preProcessor);
        return this;
    }

    // Command Post-Processor
    public ConfigBuilder<S, I> postProcessor(CommandPostProcessor<S> postProcessor) {
        config.registerGlobalPostProcessor(postProcessor);
        return this;
    }

    // Context Resolver Factory
    public ConfigBuilder<S, I> contextResolverFactory(Type type, ContextResolverFactory<S> factory) {
        config.registerContextResolverFactory(type, factory);
        return this;
    }

    // Context Resolver
    public <T> ConfigBuilder<S, I> contextResolver(Type type, ContextResolver<S, T> resolver) {
        config.registerContextResolver(type, resolver);
        return this;
    }

    // Parameter Type
    public <T> ConfigBuilder<S, I> parameterType(Type type, ParameterType<S, T> resolver) {
        config.registerParamType(type, resolver);
        return this;
    }

    // Named Suggestion Resolver
    public ConfigBuilder<S, I> namedSuggestionResolver(String name, SuggestionResolver<S> suggestionResolver) {
        config.registerNamedSuggestionResolver(name, suggestionResolver);
        return this;
    }

    // Source Resolver
    public <R> ConfigBuilder<S, I> sourceResolver(Type type, SourceResolver<S, R> sourceResolver) {
        config.registerSourceResolver(type, sourceResolver);
        return this;
    }

    // Placeholder
    public ConfigBuilder<S, I> placeholder(Placeholder<S> placeholder) {
        config.registerPlaceholder(placeholder);
        return this;
    }

    // Help Provider
    public ConfigBuilder<S, I> helpProvider(HelpProvider<S> helpProvider) {
        config.setHelpProvider(helpProvider);
        return this;
    }

    public abstract @NotNull I build();

}