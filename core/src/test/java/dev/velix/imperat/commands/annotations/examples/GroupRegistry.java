package dev.velix.imperat.commands.annotations.examples;

import dev.velix.imperat.util.Registry;

import java.util.HashMap;
import java.util.Map;

public final class GroupRegistry extends Registry<String, Group> {
    
    private final static GroupRegistry instance = new GroupRegistry();
    private final Map<String, Group> userGroups = new HashMap<>();
    
    GroupRegistry() {
        Group g = new Group("member");
        setData("member", g);
        setGroup("mqzen", g);
    }
    
    public static GroupRegistry getInstance() {
        return instance;
    }
    
    public void setGroup(String name, Group group) {
        userGroups.put(name, group);
    }
    
    public Group getGroup(String username) {
        return userGroups.get(username);
    }
    
    
}
