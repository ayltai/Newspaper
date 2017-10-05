package com.github.ayltai.newspaper.analytics;

import android.support.annotation.NonNull;

public final class ViewEvent extends Event {
    public static final String NAME = "Screen View";

    public static final String ATTRIBUTE_SCREEN_NAME = "Screen Name";
    public static final String ATTRIBUTE_SOURCE      = "Source";
    public static final String ATTRIBUTE_CATEGORY    = "Category";

    public ViewEvent() {
        super(ViewEvent.NAME);
    }

    @NonNull
    public ViewEvent setScreenName(@NonNull final String screenName) {
        this.attributes.add(new Attribute(ViewEvent.ATTRIBUTE_SCREEN_NAME, screenName));
        return this;
    }

    @NonNull
    public ViewEvent setSource(@NonNull final String source) {
        this.attributes.add(new Attribute(ViewEvent.ATTRIBUTE_SOURCE, source));
        return this;
    }

    @NonNull
    public ViewEvent setCategory(@NonNull final String category) {
        this.attributes.add(new Attribute(ViewEvent.ATTRIBUTE_CATEGORY, category));
        return this;
    }
}
