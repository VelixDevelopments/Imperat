package dev.zafrias.imperat;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@Getter
public final class ResolvingResult<T> {

	private final @Nullable T resolved;
	private final @Nullable Throwable exception;

	private ResolvingResult(@Nullable T resolvedObject, @Nullable Throwable ex) {
		if(resolvedObject == null && ex == null) {
			throw new IllegalStateException("Invalid result, cannot accept a null result without any error");
		}
		this.resolved = resolvedObject;
		this.exception = ex;
	}

	private ResolvingResult(@NotNull T resolved) {
		this(resolved, null);
	}

	public static <T> ResolvingResult<T> success(T resolvedValue) {
		return new ResolvingResult<>(resolvedValue);
	}

	public static <T> ResolvingResult<T> fail(@NotNull Throwable ex) {
		return new ResolvingResult<>(null, ex);
	}

}
