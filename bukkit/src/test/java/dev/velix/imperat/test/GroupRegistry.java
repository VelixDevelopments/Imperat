package dev.velix.imperat.test;

import dev.velix.imperat.util.Registry;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class GroupRegistry extends Registry<String, Group> {
	
	private static GroupRegistry instance;
	
	public static GroupRegistry getInstance() {
		if (instance == null)
			instance = new GroupRegistry();
		return instance;
	}
	
	private final Map<UUID, Group> userGroups = new HashMap<>();
	
	public void setGroup(UUID uuid, Group group) {
		userGroups.put(uuid, group);
	}
	
	public Group getGroup(UUID uuid) {
		return userGroups.get(uuid);
	}
	
	GroupRegistry() {
		setData("member", new Group("member"));
	}
	
	
}
