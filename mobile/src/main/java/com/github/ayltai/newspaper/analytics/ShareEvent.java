package com.github.ayltai.newspaper.analytics;

import android.support.annotation.NonNull;

public final class ShareEvent extends Event {
    public static final String NAME = "Share";

    public static final String ATTRIBUTE_SOURCE   = "Source";
    public static final String ATTRIBUTE_CATEGORY = "Category";

    public ShareEvent() {
        super(ShareEvent.NAME);
    }

    @NonNull
    public ShareEvent setSource(@NonNull final String source) {
        this.attributes.add(new Attribute(ShareEvent.ATTRIBUTE_SOURCE, source));
        return this;
    }

    @NonNull
    public ShareEvent setCategory(@NonNull final String category) {
        this.attributes.add(new Attribute(ShareEvent.ATTRIBUTE_CATEGORY, category));
        return this;
    }
}
