package dev.zafrias.imperat;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@Getter
public final class Result<T> {

	private final @Nullable T resolved;
	private final @Nullable Throwable exception;

	private Result(@Nullable T resolvedObject, @Nullable Throwable ex) {
		if (resolvedObject == null && ex == null) {
			throw new IllegalStateException("Invalid result, cannot accept a null result without any error");
		}
		this.resolved = resolvedObject;
		this.exception = ex;
	}

	private Result(@NotNull T resolved) {
		this(resolved, null);
	}

	public static <T> Result<T> success(T resolvedValue) {
		return new Result<>(resolvedValue);
	}

	public static <T> Result<T> fail(@NotNull Throwable ex) {
		return new Result<>(null, ex);
	}

}
