package dev.velix.imperat.adventure;

import dev.velix.imperat.context.Source;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.ComponentLike;

public class EmptyAdventure<S> implements AdventureProvider<S> {
	
	@Override
	public Audience audience(final S sender) {
		return null;
	}
	
	@Override
	public Audience audience(final Source source) {
		return null;
	}
	
	@Override
	public void send(final S sender, final ComponentLike component) {
		// do nothing
	}
	
	@Override
	public void send(final Source source, final ComponentLike component) {
		// do nothing
	}
	
}
