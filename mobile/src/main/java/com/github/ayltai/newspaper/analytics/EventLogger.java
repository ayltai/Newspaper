package com.github.ayltai.newspaper.analytics;

import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.util.TestUtils;

public abstract class EventLogger {
    public <T extends Event> void logEvent(@NonNull final T event) {
        if (!TestUtils.isLoggable()) {
            if (event instanceof ClickEvent) {
                this.logEvent((ClickEvent)event);
            } else if (event instanceof CountEvent) {
                this.logEvent((CountEvent)event);
            } else if (event instanceof SearchEvent) {
                this.logEvent((SearchEvent)event);
            } else if (event instanceof ShareEvent) {
                this.logEvent((ShareEvent)event);
            } else if (event instanceof ViewEvent) {
                this.logEvent((ViewEvent)event);
            } else {
                this.logCustomEvent(event);
            }
        }
    }

    protected abstract void logEvent(@NonNull AppOpenEvent event);

    protected abstract void logEvent(@NonNull ClickEvent event);

    protected abstract void logEvent(@NonNull CountEvent event);

    protected abstract void logEvent(@NonNull SearchEvent event);

    protected abstract void logEvent(@NonNull ShareEvent event);

    protected abstract void logEvent(@NonNull ViewEvent event);

    protected abstract void logCustomEvent(@NonNull Event event);
}
