package com.github.ayltai.newspaper.util;

import android.support.annotation.NonNull;

public class Optional<T> {
    public interface Action<T> {
        void apply(T value);
    }

    private final T value;

    private Optional() {
        this.value = null;
    }

    private Optional(final T value) {
        this.value = value;
    }

    public boolean isPresent() {
        return this.value != null;
    }

    public T get() {
        return this.value;
    }

    public T orElse(final T value) {
        if (this.value == null) return value;

        return this.value;
    }

    public void ifPresent(@NonNull final Action<T> action) {
        if (this.value != null) action.apply(this.value);
    }

    public static <T> Optional<T> empty() {
        return new Optional<>();
    }

    public static <T> Optional<T> of(final T value) {
        return new Optional<>(value);
    }
}
