package dev.velix.imperat.annotations.base;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.ImperatConfig;
import dev.velix.imperat.annotations.*;
import dev.velix.imperat.annotations.base.element.MethodElement;
import dev.velix.imperat.annotations.base.element.ParameterElement;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.ExecutionContext;
import dev.velix.imperat.context.Source;
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
            if (TypeUtility.areRelatedTypes(parameter.getParameterizedType(), CommandHelp.class)) {
                return true;
            }
        }
        return false;
    }


    public static <S extends Source> Object[] loadParameterInstances(
        Imperat<S> dispatcher,
        List<CommandParameter<S>> fullParameters,
        S source,
        ExecutionContext<S> context,
        MethodElement method
    ) throws ImperatException {

        Object[] paramsInstances = new Object[method.getParameters().size()];

        ParameterElement firstParam = method.getParameterAt(0);
        assert firstParam != null;

        if (!dispatcher.canBeSender(firstParam.getType())) {
            paramsInstances[0] = context.getResolvedSource(firstParam.getType());
        } else {
            paramsInstances[0] = source;
        }

        for (int i = 1, p = 0; i < method.size(); i++, p++) {
            ParameterElement actualParameter = method.getParameterAt(i);

            assert actualParameter != null;
            var contextResolver = dispatcher.config().getMethodParamContextResolver(actualParameter);

            if (contextResolver != null) {
                paramsInstances[i] = contextResolver.resolve(context, actualParameter);
                p--;
                continue;
            }

            CommandParameter<S> parameter = getUsageParam(fullParameters, p);
            if (parameter == null)
                continue;

            String name = parameter.name();
            if (parameter.isFlag()) {
                paramsInstances[i] = context.getFlagValue(name);
            } else {
                paramsInstances[i] = context.getArgument(name);
            }

        }
        return paramsInstances;
    }

    private static <S extends Source> @Nullable CommandParameter<S> getUsageParam(List<? extends CommandParameter<S>> params, int index) {
        if (index < 0 || index >= params.size()) return null;
        return params.get(index);
    }

    public static <S extends Source> @NotNull String getParamName(
        ImperatConfig<S> imperat,
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

        return imperat.replacePlaceholders(name);
    }

    public static <S extends Source> @NotNull String getParamName(ImperatConfig<S> imperat, Parameter parameter) {
        return getParamName(
            imperat,
            parameter,
            parameter.getAnnotation(Named.class),
            parameter.getAnnotation(Flag.class),
            parameter.getAnnotation(Switch.class));
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
        if (!TypeUtility.matches(valueSupplier.reflectionType(), parameter.getType())) {
            throw new IllegalArgumentException("Optional supplier of value-valueType '" + valueSupplier.reflectionType().getTypeName() + "' doesn't match the optional value valueType '" + parameter.getType().getName() + "'");
        }

        return valueSupplier;
    }

    @SuppressWarnings("unchecked")
    public static <T> @NotNull OptionalValueSupplier<T> deduceOptionalValueSupplier(
        Parameter parameter,
        Default defaultAnnotation,
        DefaultProvider provider,
        OptionalValueSupplier<T> fallback
    ) {

        if (defaultAnnotation != null) {
            String def = defaultAnnotation.value();
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
        return fallback;
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


    public static boolean isHelpParameter(Parameter element) {
        return TypeUtility.areRelatedTypes(element.getType(), CommandHelp.class);
    }
}
