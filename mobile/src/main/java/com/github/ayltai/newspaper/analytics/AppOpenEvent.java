package com.github.ayltai.newspaper.analytics;

public final class AppOpenEvent extends Event {
    public static final String NAME = "App Open";

    public AppOpenEvent() {
        super(AppOpenEvent.NAME);
    }
}
