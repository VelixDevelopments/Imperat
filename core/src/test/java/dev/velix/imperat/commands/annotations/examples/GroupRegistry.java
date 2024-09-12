package dev.velix.imperat.commands.annotations.examples;

import dev.velix.imperat.util.Registry;

import java.util.HashMap;
import java.util.Map;

public final class GroupRegistry extends Registry<String, Group> {
    
    private static GroupRegistry instance;
    private final Map<String, Group> userGroups = new HashMap<>();
    
    GroupRegistry() {
        setData("mqzen", new Group("member"));
    }
    
    public static GroupRegistry getInstance() {
        if (instance == null)
            instance = new GroupRegistry();
        return instance;
    }
    
    public void setGroup(String name, Group group) {
        userGroups.put(name, group);
    }
    
    public Group getGroup(String name) {
        return userGroups.get(name);
    }
    
    
}
