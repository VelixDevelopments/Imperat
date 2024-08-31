package dev.velix.imperat.annotations;

import dev.velix.imperat.util.CommandDebugger;
import dev.velix.imperat.Imperat;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.command.CommandExecution;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.internal.ResolvedFlag;
import dev.velix.imperat.exceptions.CommandException;
import dev.velix.imperat.help.CommandHelp;
import dev.velix.imperat.util.asm.DefaultMethodCallerFactory;
import dev.velix.imperat.util.asm.MethodCaller;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

@ApiStatus.Internal
public final class MethodCommandExecutor<C> implements CommandExecution<C> {

    private final Object proxy;
    private final Imperat<C> dispatcher;
    private final Method method;
    private final MethodCaller.BoundMethodCaller boundMethodCaller;
    private final List<CommandParameter> fullParameters;
    //private final Help helpAnnotation;

    public MethodCommandExecutor(Object proxy,
                                 Imperat<C> dispatcher,
                                 Method method,
                                 List<CommandParameter> fullParameters
            /*Help help*/) {
        this.proxy = proxy;
        this.dispatcher = dispatcher;
        this.method = method;
        try {
            boundMethodCaller = DefaultMethodCallerFactory.INSTANCE.createFor(method).bindTo(proxy);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        this.fullParameters = fullParameters;
        //this.helpAnnotation = help;
    }

    @SuppressWarnings("unchecked")
    public static <C> Object[] loadParameterInstances(Imperat<C> dispatcher,
                                                      List<CommandParameter> fullParameters,
                                                      Source<C> source,
                                                      ExecutionContext context,
                                                      Method method,
                                                      @Nullable CommandHelp<C> commandHelp) throws CommandException {
        Parameter[] parameters = method.getParameters();
        Object[] paramsInstances = new Object[parameters.length];

        paramsInstances[0] = source;

        for (int i = 1, p = 0; i < parameters.length; i++, p++) {
            Parameter actualParameter = parameters[i];
            if (commandHelp != null && CommandHelp.class.isAssignableFrom(actualParameter.getType())) {
                paramsInstances[i] = commandHelp;
                p--;
                continue;
            }

            var factory = dispatcher.getContextResolverFactory();
            var contextResolver = factory.create(actualParameter);

            if (contextResolver != null) {
                System.out.println("PARAMETER AT " + p + "GOT CONTEXT RESOLVED");
                paramsInstances[i] = contextResolver.resolve((Context<C>) context, actualParameter);
                continue;
            }

            contextResolver = dispatcher.getContextResolver(actualParameter.getType());
            if (contextResolver != null) {
                paramsInstances[i] = contextResolver.resolve((Context<C>) context, actualParameter);
                continue;
            }

            CommandParameter parameter = getUsageParam(fullParameters, p);
            if (parameter == null)
                continue;

            if (parameter.isFlag()) {
                ResolvedFlag value = context.getFlag(parameter.getName());
                paramsInstances[i] = value.value();
            } else {
                Object value = context.getArgument(parameter.getName());
                paramsInstances[i] = value;
            }

        }

        return paramsInstances;
    }

    /**
     * Executes the command's actions
     *
     * @param source  the source/sender of this command
     * @param context the context of the command
     */
    @Override
    public void execute(Source<C> source,
                        ExecutionContext context) throws CommandException {

        var instances = loadParameterInstances(dispatcher, fullParameters,
                source, context, method, null);

        try {
            boundMethodCaller.call(instances);
        } catch (Exception ex) {
            CommandDebugger.error(proxy.getClass(), method.getName(), ex);
        }

    }


    private static @Nullable CommandParameter getUsageParam(List<? extends CommandParameter> params, int index) {
        if (index < 0 || index >= params.size()) return null;
        return params.get(index);
    }

}
