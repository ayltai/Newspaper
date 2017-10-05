package com.github.ayltai.newspaper.analytics;

import android.support.annotation.NonNull;

public final class ClickEvent extends Event {
    public static final String NAME = "Click";

    public static final String ATTRIBUTE_ELEMENT_NAME = "Element Name";

    public ClickEvent() {
        super(ClickEvent.NAME);
    }

    @NonNull
    public ClickEvent setElementName(@NonNull final String elementName) {
        this.attributes.add(new Attribute(ClickEvent.ATTRIBUTE_ELEMENT_NAME, elementName));
        return this;
    }
}
