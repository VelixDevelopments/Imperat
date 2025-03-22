package io.papermc.paper.command.brigadier;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.lifecycle.event.registrar.Registrar;
import org.jetbrains.annotations.*;

import java.util.Collection;
import java.util.Set;

public interface Commands extends Registrar {

    static LiteralArgumentBuilder<CommandSourceStack> literal(String literal) {
        return LiteralArgumentBuilder.literal(literal);
    }

    static <T> RequiredArgumentBuilder<CommandSourceStack, T> argument(String name, ArgumentType<T> argumentType) {
        return RequiredArgumentBuilder.argument(name, argumentType);
    }

    CommandDispatcher<CommandSourceStack> getDispatcher();

    default @Unmodifiable Set<String> register(LiteralCommandNode<CommandSourceStack> node) {
        return null;
    }

    default @Unmodifiable Set<String> register(LiteralCommandNode<CommandSourceStack> node, @Nullable String description) {
        return null;
    }

    default @Unmodifiable Set<String> register(LiteralCommandNode<CommandSourceStack> node, Collection<String> aliases) {
        return null;
    }

    @Unmodifiable
    Set<String> register(LiteralCommandNode<CommandSourceStack> var1, @Nullable String var2, Collection<String> var3);

    @Unmodifiable
    Set<String> register(PluginMeta var1, LiteralCommandNode<CommandSourceStack> var2, @Nullable String var3, Collection<String> var4);

}
