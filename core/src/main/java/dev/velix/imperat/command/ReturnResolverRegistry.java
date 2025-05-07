package dev.velix.imperat.command;

import dev.velix.imperat.command.returns.ReturnResolver;
import dev.velix.imperat.context.Source;
import dev.velix.imperat.util.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@ApiStatus.AvailableSince("1.0.0")
public final class ReturnResolverRegistry<S extends Source> extends Registry<Type, ReturnResolver<S, ?>> {

    public static <S extends Source> ReturnResolverRegistry<S> createDefault() {
        return new ReturnResolverRegistry<>();
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable ReturnResolver<S, T> getReturnResolver(Type type) {
        return (ReturnResolver<S, T>) getData(type).orElse(null);
    }

}
