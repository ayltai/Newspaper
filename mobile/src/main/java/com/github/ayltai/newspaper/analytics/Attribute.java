package com.github.ayltai.newspaper.analytics;

import android.support.annotation.NonNull;

public final class Attribute {
    private final String name;
    private final String value;

    public Attribute(@NonNull final String name, @NonNull final String value) {
        this.name  = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
