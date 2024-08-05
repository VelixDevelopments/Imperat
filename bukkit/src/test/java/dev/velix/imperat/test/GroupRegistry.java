package dev.velix.imperat.test;

import dev.velix.imperat.util.Registry;

public final class GroupRegistry extends Registry<String, Group> {

	private static GroupRegistry instance;

	public static GroupRegistry getInstance() {
		if(instance == null)
			instance = new GroupRegistry();
		return instance;
	}

	GroupRegistry() {
		setData("member", new Group("member"));
	}
}
