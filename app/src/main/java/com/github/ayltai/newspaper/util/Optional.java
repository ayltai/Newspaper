package com.github.ayltai.newspaper.util;

import java.util.NoSuchElementException;

import javax.annotation.Nonnull;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class Optional<T> {
    private final T value;

    private Optional() {
        this.value = null;
    }

    private Optional(@Nonnull @NonNull @lombok.NonNull final T value) {
        this.value = value;
    }

    public boolean isPresent() {
        return this.value != null;
    }

    @Nonnull
    @NonNull
    public T get() {
        if (this.value == null) throw new NoSuchElementException();

        return this.value;
    }

    public T orElse(@Nullable final T other) {
        return this.isPresent() ? this.get() : other;
    }

    public static <T> Optional<T> empty() {
        return new Optional<>();
    }

    public static <T> Optional<T> of(@Nonnull @NonNull @lombok.NonNull final T value) {
        return new Optional<>(value);
    }

    public static <T> Optional<T> ofNullable(@Nullable final T value) {
        return value == null ? Optional.empty() : Optional.of(value);
    }
}
