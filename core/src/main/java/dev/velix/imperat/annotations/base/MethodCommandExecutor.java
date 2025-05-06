package dev.velix.imperat.annotations.base;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.base.element.ClassElement;
import dev.velix.imperat.annotations.base.element.MethodElement;
import dev.velix.imperat.command.CommandExecution;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.returns.ReturnResolver;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.util.ImperatDebugger;
import dev.velix.imperat.util.asm.DefaultMethodCallerFactory;
import dev.velix.imperat.util.asm.MethodCaller;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Internal
public class MethodCommandExecutor<S extends Source> implements CommandExecution<S> {

    private final Imperat<S> dispatcher;
    private final ClassElement methodOwner;
    private final MethodElement method;
    private final MethodCaller.BoundMethodCaller boundMethodCaller;
    private final List<CommandParameter<S>> fullParameters;

    private MethodCommandExecutor(
        Imperat<S> dispatcher,
        MethodElement method,
        List<CommandParameter<S>> fullParameters
    ) {

        try {
            this.dispatcher = dispatcher;
            this.method = method;

            methodOwner = (ClassElement) method.getParent();
            assert methodOwner != null;
            boundMethodCaller = DefaultMethodCallerFactory.INSTANCE.createFor(method.getElement()).bindTo(methodOwner.getObjectInstance());

            this.fullParameters = fullParameters;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        //this.helpAnnotation = help;
    }

    public static <S extends Source> MethodCommandExecutor<S> of(
        Imperat<S> imperat,
        MethodElement method,
        List<CommandParameter<S>> fullParameters
    ) {
        return new MethodCommandExecutor<>(imperat, method, fullParameters);
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

        ImperatDebugger.debugForTesting("Debugging params:-");
        for (var param : fullParameters) {
            ImperatDebugger.debugForTesting("-%s", param.format());
        }
        ImperatDebugger.debugForTesting("SIZE=%s", fullParameters.size());

        var instances = AnnotationHelper.loadParameterInstances(
            dispatcher, fullParameters,
            source, context, method
        );
        for (int i = 0; i < instances.length; i++) {
            var instance = instances[i];
            ImperatDebugger.debugForTesting("Object #%s = '%s', type='%s'", i, instance, instance != null ? instances[i].getClass().getName() : "N/A");
        }

        try {
            Object returned = boundMethodCaller.call(instances);
            if (method.getReturnType() == void.class) {
                return;
            }

            ReturnResolver<S, Object> returnResolver = context.imperatConfig().getReturnResolver(method.getReturnType());
            if (returnResolver == null) {
                return;
            }

            returnResolver.handle(context, returned);
        } catch (Exception ex) {
            ImperatDebugger.error(methodOwner.getElement(), method.getElement().getName(), ex);
        }

    }

}
