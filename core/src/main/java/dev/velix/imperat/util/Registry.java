package dev.velix.imperat.util;

import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ApiStatus.Internal
public class Registry<K, V> {

	private final Map<K, V> data;

	public Registry(Supplier<Map<K, V>> data) {
		this.data = data.get();
	}

	public Registry() {
		this(HashMap::new);
	}

	public Optional<V> getData(K key) {
		return Optional.ofNullable(data.get(key));
	}

	public void setData(K key, V value) {
		data.put(key, value);
	}

	public void updateData(K key, Consumer<V> valueUpdater) {
		data.computeIfPresent(key, (k, v) -> {
			valueUpdater.accept(v);
			return v;
		});
	}

	public Optional<V> search(BiPredicate<K, V> predicate) {
		for (Map.Entry<K, V> entry : data.entrySet()) {
			if (predicate.test(entry.getKey(), entry.getValue()))
				return Optional.ofNullable(entry.getValue());
		}
		return Optional.empty();
	}

	public Registry<K, V> addAll(Registry<K, V> registry) {
		this.data.putAll(registry.data);
		return this;
	}

	public Collection<? extends V> getAll() {
		return data.values();
	}
}
