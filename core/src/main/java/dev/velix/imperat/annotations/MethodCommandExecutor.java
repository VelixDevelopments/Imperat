package dev.velix.imperat.annotations;

import dev.velix.imperat.annotations.injectors.context.ProxyCommand;
import dev.velix.imperat.util.CommandDebugger;
import dev.velix.imperat.Imperat;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.command.CommandExecution;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.exceptions.CommandException;
import dev.velix.imperat.util.asm.DefaultMethodCallerFactory;
import dev.velix.imperat.util.asm.MethodCaller;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Method;
import java.util.List;

@ApiStatus.Internal
public final class MethodCommandExecutor<C> implements CommandExecution<C> {

    private final ProxyCommand<C> proxy;
    private final Imperat<C> dispatcher;
    private final Method method;
    private final MethodCaller.BoundMethodCaller boundMethodCaller;
    private final List<CommandParameter> fullParameters;
    //private final Help helpAnnotation;

    public MethodCommandExecutor(ProxyCommand<C> proxyCommand,
                                 Imperat<C> dispatcher,
                                 Method method,
                                 List<CommandParameter> fullParameters) {
        this.proxy = proxyCommand;
        this.dispatcher = dispatcher;
        this.method = method;
        try {
            boundMethodCaller = DefaultMethodCallerFactory.INSTANCE.createFor(method).bindTo(proxy.proxyInstance());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        this.fullParameters = fullParameters;
        //this.helpAnnotation = help;
    }
    
    /**
     * Executes the command's actions
     *
     * @param source  the source/sender of this command
     * @param context the context of the command
     */
    @Override
    public void execute(Source<C> source,
                        ExecutionContext<C> context) throws CommandException {

        var instances = AnnotationHelper.loadParameterInstances(dispatcher, fullParameters,
                source, context, method, null);

        try {
            boundMethodCaller.call(instances);
        } catch (Exception ex) {
            CommandDebugger.error(proxy.getClass(), method.getName(), ex);
        }

    }
    
}
