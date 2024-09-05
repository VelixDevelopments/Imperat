package dev.velix.imperat.annotations.injectors.context;

import dev.velix.imperat.command.Command;

public record ProxyCommand<C>(Class<?> proxyClass, Object proxyInstance, Command<C> commandLoaded) {


}
