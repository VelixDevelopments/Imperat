package dev.velix.imperat.annotations.base.element.selector;

import dev.velix.imperat.annotations.base.element.ClassElement;
import dev.velix.imperat.annotations.base.element.MethodElement;
import dev.velix.imperat.annotations.base.element.ParameterElement;
import dev.velix.imperat.context.Source;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public interface MethodRules {

    Rule<MethodElement> IS_PUBLIC = Rule.buildForMethod()
        .condition((imperat, registry, method) -> Modifier.isPublic(method.getModifiers()))
        .failure((registry, method) -> {
            throw methodError(method, "is not public");
        })
        .build();

    Rule<MethodElement> RETURNS_VOID = Rule.buildForMethod()
        .condition((imperat, registry, method) -> method.getReturnType() == void.class)
        .failure((registry, method) -> {
            throw methodError(method, "should return void");
        })
        .build();

    Rule<MethodElement> HAS_KNOWN_SENDER = Rule.buildForMethod()
        .condition((imperat, registry, method) -> {
            ParameterElement parameterElement = method.getParameterAt(0);
            if (parameterElement == null) return false;
            return imperat.canBeSender(parameterElement.getType())
                || imperat.hasSourceResolver(parameterElement.getType());
        })
        .failure((registry, method) -> {
            ParameterElement parameterElement = method.getParameterAt(0);
            String msg;
            if (parameterElement == null) {
                msg = "Method '" + method.getName() + "' has no parameters";
            } else {
                msg = "First parameter of type '" + parameterElement.getType().getTypeName() + "' is not a sub-type of `" + Source.class.getName() + "'";
            }
            throw methodError(method, msg);
        })
        .build();

    Rule<MethodElement> HAS_LEAST_ONLY_ONE_MAIN_ANNOTATION = Rule.buildForMethod()
        .condition((imperat, registry, element) -> {
            long count = Arrays.stream(element.getDeclaredAnnotations())
                .filter(annotation -> registry.isEntryPointAnnotation(annotation.annotationType())).count();
            return count == 1;
        })
        .failure((registry, element) -> {
            StringBuilder builder = new StringBuilder();

            Annotation[] declaredAnnotations = element.getDeclaredAnnotations();
            for (int i = 0, declaredAnnotationsLength = declaredAnnotations.length; i < declaredAnnotationsLength; i++) {
                Annotation annotation = declaredAnnotations[i];
                if (registry.isEntryPointAnnotation(annotation.annotationType())) {
                    builder.append("@").append(annotation.annotationType().getSimpleName());
                }

                if (i != declaredAnnotationsLength - 1) {
                    builder.append(", ");
                }
            }

            throw methodError(element, "has more than one main annotations such as: `" + builder + "`");
        })
        .build();

    private static IllegalStateException methodError(MethodElement element, String msg) {
        ClassElement parent = (ClassElement) element.getParent();
        assert parent != null;

        return new IllegalStateException(
            String.format("Method '%s' In class '%s' " + msg,
                element.getElement().getName(), parent.getElement().getName()
            )
        );
    }
}
