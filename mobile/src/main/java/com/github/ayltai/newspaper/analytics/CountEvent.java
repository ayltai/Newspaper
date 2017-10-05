package com.github.ayltai.newspaper.analytics;

import android.support.annotation.NonNull;

public final class CountEvent extends Event {
    public static final String NAME = "Count (%s)";

    public static final String ATTRIBUTE_COUNTER_NAME = "Count";

    public CountEvent(@NonNull final String counterName, final int count) {
        super(String.format(CountEvent.NAME, counterName));
        this.attributes.add(new Attribute(CountEvent.ATTRIBUTE_COUNTER_NAME, String.valueOf(count)));
    }
}
