package com.github.ayltai.newspaper.analytics;

public final class ClickEvent extends Event {
    public static final String NAME = "Click";

    public static final String ATTRIBUTE_ELEMENT_NAME = "Element Name";

    public ClickEvent() {
        super(ClickEvent.NAME);
    }
}
