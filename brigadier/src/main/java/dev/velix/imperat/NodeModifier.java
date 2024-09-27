package dev.velix.imperat;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;

import java.lang.reflect.Field;
import java.util.function.Predicate;

final class NodeModifier {
    
    private static final Field COMMAND_NODE;
    
    private final static Field REQUIREMENT;
    
    private final static Field SUGGESTIONS;
    
    
    static {
        try {
            COMMAND_NODE = CommandNode.class.getDeclaredField("command");
            COMMAND_NODE.setAccessible(true);
            
            REQUIREMENT = CommandNode.class.getDeclaredField("requirement");
            REQUIREMENT.setAccessible(true);
            
            SUGGESTIONS = ArgumentCommandNode.class.getDeclaredField("customSuggestions");
            SUGGESTIONS.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static <T> void setCommand(CommandNode<T> node, Command<T> brigadierCmd) {
        try {
            NodeModifier.COMMAND_NODE.set(node, brigadierCmd);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    public static <T> void setRequirement(CommandNode<T> node, Predicate<T> predicate) {
        try {
            NodeModifier.REQUIREMENT.set(node, predicate);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    public static <S, T> void setSuggestionProvider(ArgumentCommandNode<S, T> argCmdNode,
                                                    SuggestionProvider<S> provider) {
        try {
            SUGGESTIONS.set(argCmdNode, provider);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
