package dev.velix.imperat.util.jeflect.lambdas;

/**
 * A universal interface suitable for calling any variant of the method.
 */
@FunctionalInterface
public interface Lambda {
    /**
     * Calls a method belonging to the specified object with the specified parameters.
     *
     * @param instance an object containing the implementation of the method
     * @param args     method parameters
     * @return the result returned by the method
     * @throws Throwable if the method throws an exception
     */
    Object invoke(Object instance, Object[] args) throws Throwable;

    /**
     * Calls a static method with the specified parameters.
     *
     * @param args method parameters
     * @return the result returned by the method
     * @throws Throwable if the method throws an exception
     */
    default Object invoke(Object[] args) throws Throwable {
        return invoke(null, args);
    }

    /**
     * Calls a method belonging to the specified object without parameters.
     *
     * @param instance an object containing the implementation of the method
     * @return the result returned by the method
     * @throws Throwable if the method throws an exception
     */
    default Object invoke(Object instance) throws Throwable {
        return invoke(instance, null);
    }

    /**
     * Calls a static method without parameters.
     *
     * @return the result returned by the method
     * @throws Throwable if the method throws an exception
     */
    default Object invoke() throws Throwable {
        return invoke(null, null);
    }
}
