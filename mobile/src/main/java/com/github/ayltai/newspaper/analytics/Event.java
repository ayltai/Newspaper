package com.github.ayltai.newspaper.analytics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class Event {
    protected final List<Attribute> attributes = new ArrayList<>();

    private final String name;

    public Event(@NonNull final String name) {
        this.name = name;
    }

    @NonNull
    public String getName() {
        return this.name;
    }

    public Collection<Attribute> getAttributes() {
        return Collections.unmodifiableList(this.attributes);
    }

    @Nullable
    public Attribute getAttribute(@NonNull final String attributeName) {
        for (final Attribute attribute : this.attributes) {
            if (attributeName.equals(attribute.getName())) return attribute;
        }

        return null;
    }
}
