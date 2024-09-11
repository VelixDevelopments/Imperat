package dev.velix.imperat.annotations;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.types.*;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.context.internal.ResolvedFlag;
import dev.velix.imperat.exception.ImperatException;
import dev.velix.imperat.help.CommandHelp;
import dev.velix.imperat.supplier.OptionalValueSupplier;
import dev.velix.imperat.util.TypeUtility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

public final class AnnotationHelper {
    
    public static boolean isMethodHelp(Method method) {
        for (Parameter parameter : method.getParameters()) {
            if (TypeUtility.areRelatedTypes(parameter.getType(), CommandHelp.class)) {
                return true;
            }
        }
        return false;
    }
    
    
    public static <S extends Source> Object[] loadParameterInstances(
            Imperat<S> dispatcher,
            List<CommandParameter> fullParameters,
            S source,
            ExecutionContext<S> context,
            Method method,
            @Nullable CommandHelp<S> commandHelp) throws ImperatException {
        Parameter[] parameters = method.getParameters();
        Object[] paramsInstances = new Object[parameters.length];
        
        paramsInstances[0] = source;
        for (int i = 1, p = 0; i < parameters.length; i++, p++) {
            Parameter actualParameter = parameters[i];
            if (commandHelp != null && TypeUtility.areRelatedTypes(CommandHelp.class, actualParameter.getType())) {
                paramsInstances[i] = commandHelp;
                p--;
                continue;
            }
            
            var factory = dispatcher.getContextResolverFactory();
            var contextResolver = factory.create(actualParameter);
            
            if (contextResolver != null) {
                System.out.println("PARAMETER AT " + p + "GOT CONTEXT RESOLVED");
                paramsInstances[i] = contextResolver.resolve((Context<S>) context, actualParameter);
                continue;
            }
            
            contextResolver = dispatcher.getContextResolver(actualParameter.getType());
            if (contextResolver != null) {
                paramsInstances[i] = contextResolver.resolve((Context<S>) context, actualParameter);
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
    
    private static @Nullable CommandParameter getUsageParam(List<? extends CommandParameter> params, int index) {
        if (index < 0 || index >= params.size()) return null;
        return params.get(index);
    }
    
    public static @NotNull String getParamName(
            Parameter parameter,
            @Nullable Named named,
            @Nullable Flag flag,
            @Nullable Switch switchAnnotation
    ) {
        String name;
        
        if (named != null)
            name = named.value();
        else if (flag != null)
            name = flag.value()[0];
        else if (switchAnnotation != null)
            name = switchAnnotation.value()[0];
        else
            name = parameter.getName();
        
        return name;
    }
    
    public static @NotNull String getParamName(Parameter parameter) {
        return getParamName(
                parameter,
                parameter.getAnnotation(Named.class),
                parameter.getAnnotation(Flag.class),
                parameter.getAnnotation(Switch.class)
        );
    }
    
    @SuppressWarnings({"unchecked"})
    public static <T> @NotNull OptionalValueSupplier<T> getOptionalValueSupplier(
            Parameter parameter,
            Class<? extends OptionalValueSupplier<?>> supplierClass
    ) throws NoSuchMethodException, InstantiationException,
            IllegalAccessException, InvocationTargetException {
        
        var emptyConstructor = supplierClass.getDeclaredConstructor();
        emptyConstructor.setAccessible(true);
        OptionalValueSupplier<T> valueSupplier = (OptionalValueSupplier<T>) emptyConstructor.newInstance();
        if (!TypeUtility.matches(valueSupplier.getValueType(), parameter.getType())) {
            throw new IllegalArgumentException("Optional supplier of value-type '" + valueSupplier.getValueType().getName() + "' doesn't match the optional value type '" + parameter.getType().getName() + "'");
        }
        
        return valueSupplier;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> @Nullable OptionalValueSupplier<T> deduceOptionalValueSupplier(
            Parameter parameter,
            DefaultValue defaultValueAnnotation,
            DefaultValueProvider provider
    ) {
        
        if (defaultValueAnnotation != null) {
            String def = defaultValueAnnotation.value();
            return (OptionalValueSupplier<T>) OptionalValueSupplier.of(def);
        } else if (provider != null) {
            Class<? extends OptionalValueSupplier<?>> supplierClass = provider.value();
            try {
                return getOptionalValueSupplier(parameter, supplierClass);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new IllegalAccessError("Optional value suppler class '" +
                        supplierClass.getName() + "' doesn't have an empty accessible constructor !");
            }
        }
        return null;
    }
    
    public static int loadMethodPriority(Method method) {
        int count = method.getParameterCount();
        if (AnnotationHelper.isMethodHelp(method)) {
            count--;
        }
        
        if (method.isAnnotationPresent(Usage.class)) {
            //if default -> -1 else -> 0;
            return count == 1 ? -1 : 0;
        } else if (method.isAnnotationPresent(SubCommand.class)) {
            return 2 + count;
        }
        return 100;
    }
}
