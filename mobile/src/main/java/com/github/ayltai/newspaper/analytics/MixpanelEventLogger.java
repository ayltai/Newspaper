package com.github.ayltai.newspaper.analytics;

import javax.inject.Singleton;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import com.github.ayltai.newspaper.util.TestUtils;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

@Singleton
public class MixpanelEventLogger extends EventLogger {
    private MixpanelAPI api;

    public MixpanelEventLogger(@NonNull final Context context) {
        this.api = MixpanelAPI.getInstance(context, "23357b9291bde449aa7c64ef85bdff47");
    }

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
    }

    @Override
    protected void logCustomEvent(@NonNull final Event event) {
        final JSONObject parameters = new JSONObject();

        for (final Attribute attribute : event.getAttributes()) {
            try {
                parameters.put(attribute.getName(), attribute.getValue());
            } catch (final JSONException e) {
                if (TestUtils.isLoggable()) Log.w(this.getClass().getSimpleName(), e.getMessage(), e);
            }
        }

        if (!TestUtils.isRunningTests()) {
            this.api.track(event.getName(), parameters);
            this.api.flush();
        }
    }
}
