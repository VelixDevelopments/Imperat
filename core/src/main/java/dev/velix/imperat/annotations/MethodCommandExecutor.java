package dev.velix.imperat.annotations;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.element.ClassElement;
import dev.velix.imperat.annotations.element.MethodElement;
import dev.velix.imperat.annotations.element.RootCommandClass;
import dev.velix.imperat.command.CommandExecution;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.util.CommandDebugger;
import dev.velix.imperat.util.asm.DefaultMethodCallerFactory;
import dev.velix.imperat.util.asm.MethodCaller;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Internal
public final class MethodCommandExecutor<S extends Source> implements CommandExecution<S> {
    
    private final RootCommandClass<S> proxy;
    private final Imperat<S> dispatcher;
    private final MethodElement method;
    private final MethodCaller.BoundMethodCaller boundMethodCaller;
    private final List<CommandParameter> fullParameters;
    //private final Help helpAnnotation;
    
    public MethodCommandExecutor(RootCommandClass<S> rootCommandClass,
                                 Imperat<S> dispatcher,
                                 MethodElement method,
                                 List<CommandParameter> fullParameters) {
        this.proxy = rootCommandClass;
        this.dispatcher = dispatcher;
        this.method = method;
        try {
            ClassElement methodOwner = (ClassElement) method.getParent();
            assert methodOwner != null;
            
            boundMethodCaller = DefaultMethodCallerFactory.INSTANCE.createFor(method.getElement()).bindTo(methodOwner.newInstance());
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
    public void execute(S source,
                        ExecutionContext<S> context) throws ImperatException {
        
        var instances = AnnotationHelper.loadParameterInstances(
                dispatcher, fullParameters,
                source, context, method.getElement(), null
        );
        
        try {
            boundMethodCaller.call(instances);
        } catch (Exception ex) {
            ex.printStackTrace();
            CommandDebugger.error(proxy.proxyClass(), method.getElement().getName(), ex);
        }
        
    }
    
}
