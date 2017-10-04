package com.github.ayltai.newspaper.analytics;

public final class CountEvent extends Event {
    public static final String NAME = "Count";

    public static final String ATTRIBUTE_COUNTER_NAME = "Counter Name";

    public CountEvent() {
        super(CountEvent.NAME);
    }
}
