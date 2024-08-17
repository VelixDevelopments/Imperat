package dev.velix.imperat.test;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter
public final class Guild {
	
	private final String name;
	
	private final List<UUID> members = new ArrayList<>();
	
	public boolean hasMember(UUID uuid) {
		return members.contains(uuid);
	}
	
	public void addMember(UUID uuid) {
		this.members.add(uuid);
	}
	
	public void removeMember(UUID uuid) {
		this.members.remove(uuid);
	}
	
	public void sendInvite(Player inviter, Player target) {
		//TODO implement
		
	}
}
