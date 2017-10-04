package com.github.ayltai.newspaper.analytics;

public final class ViewEvent extends Event {
    public static final String NAME = "Screen View";

    public static final String ATTRIBUTE_SCREEN_NAME = "Screen Name";
    public static final String ATTRIBUTE_SOURCE      = "Source";
    public static final String ATTRIBUTE_CATEGORY    = "Category";

    public ViewEvent() {
        super(ViewEvent.NAME);
    }
}
