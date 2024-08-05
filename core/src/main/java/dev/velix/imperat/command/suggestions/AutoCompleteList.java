package dev.velix.imperat.command.suggestions;

import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

@Getter
@ApiStatus.Internal
final class AutoCompleteList {

	private final List<String> results = new ArrayList<>();

	public void add(String result) {
		if(results.contains(result)) return;
		results.add(result);
	}

	public void addAll(List<String> results) {
		for(String res : results) {
			if(this.results.contains(res)) continue;
			this.results.add(res);
		}
	}

}
