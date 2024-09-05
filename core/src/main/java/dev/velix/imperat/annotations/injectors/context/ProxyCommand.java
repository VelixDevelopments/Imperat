package dev.velix.imperat.annotations.injectors.context;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.context.Source;

public record ProxyCommand<S extends Source>(Class<?> proxyClass, Object proxyInstance, Command<S> commandLoaded) {


}
