package com.github.ayltai.newspaper.analytics;

public final class ShareEvent extends Event {
    public static final String NAME = "Share";

    public static final String ATTRIBUTE_SOURCE   = "Source";
    public static final String ATTRIBUTE_CATEGORY = "Category";

    public ShareEvent() {
        super(ShareEvent.NAME);
    }
}
