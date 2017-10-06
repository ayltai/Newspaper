package com.github.ayltai.newspaper.analytics;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import android.support.annotation.NonNull;

import com.flurry.android.FlurryAgent;

@Singleton
final class FlurryEventLogger extends EventLogger {
    @Override
    protected void logEvent(@NonNull final AppOpenEvent event) {
        this.logCustomEvent(event);
    }

    @Override
    protected void logEvent(@NonNull final ClickEvent event) {
        this.logCustomEvent(event);
    }

    @Override
    protected void logEvent(@NonNull final CountEvent event) {
        this.logCustomEvent(event);
    }

    @Override
    protected void logEvent(@NonNull final SearchEvent event) {
        this.logCustomEvent(event);
    }

    @Override
    protected void logEvent(@NonNull final ShareEvent event) {
        this.logCustomEvent(event);
    }

    @Override
    protected void logEvent(@NonNull final ViewEvent event) {
        this.logCustomEvent(event);

        FlurryAgent.onPageView();
    }

    @Override
    protected void logCustomEvent(@NonNull final Event event) {
        final Map<String, String> parameters = new HashMap<>(event.getAttributes().size());
        for (final Attribute attribute : event.getAttributes()) parameters.put(attribute.getName(), attribute.getValue());

        FlurryAgent.logEvent(event.getName(), parameters);
    }
}
