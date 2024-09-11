package dev.velix.imperat.help;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.AnnotationHelper;
import dev.velix.imperat.annotations.element.RootCommandClass;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.util.asm.DefaultMethodCallerFactory;
import dev.velix.imperat.util.asm.MethodCaller;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;

public final class MethodHelpExecution<S extends Source> implements HelpExecution<S> {
    
    private final Imperat<S> dispatcher;
    private final Method method;
    private final MethodCaller.BoundMethodCaller caller;
    private final List<CommandParameter> params;
    
    public MethodHelpExecution(Imperat<S> dispatcher,
                               RootCommandClass<S> proxy,
                               Method method,
                               List<CommandParameter> params) {
        this.dispatcher = dispatcher;
        this.method = method;
        this.params = params;
        try {
            this.caller = DefaultMethodCallerFactory.INSTANCE
                    .createFor(method).bindTo(proxy.proxyInstance());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Displays a help menu showing all possible syntaxes
     *
     * @param source the source of this execution
     * @param help   the help object
     * @param page   the page of the help menu
     */
    @Override
    public void help(S source,
                     Context<S> context,
                     CommandHelp<S> help,
                     @Nullable Integer page) throws ImperatException {
        
        Object[] instances = AnnotationHelper
                .loadParameterInstances(dispatcher, params, source, context, method, help);
        /*System.out.println("INSTANCES SIZE= " + instances.length);
        for (Object object : instances) {
            System.out.println("HELLO");
            if (object == null) {
                System.out.println("OBJECT IS NULL");
                continue;
            }
            System.out.println("INSTANCE -> " + object.getClass().getSimpleName());
        }
        System.out.println("THEN");*/
        
        caller.call(instances);
    }
    
    
}
