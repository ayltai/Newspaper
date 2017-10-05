package com.github.ayltai.newspaper.analytics;

import javax.inject.Singleton;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.firebase.analytics.FirebaseAnalytics;

@Singleton
final class FirebaseEventLogger extends EventLogger {
    private final Context context;

    FirebaseEventLogger(@NonNull final Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    protected void logEvent(@NonNull final AppOpenEvent event) {
        FirebaseAnalytics.getInstance(this.context).logEvent(FirebaseAnalytics.Event.APP_OPEN, new Bundle());
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
        final Attribute query      = event.getAttribute(SearchEvent.ATTRIBUTE_QUERY);
        final Attribute screenName = event.getAttribute(SearchEvent.ATTRIBUTE_SCREEN_NAME);

        if (query != null && screenName != null) {
            final Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SEARCH_TERM, query.getValue());
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, screenName.getValue());

            FirebaseAnalytics.getInstance(this.context).logEvent(FirebaseAnalytics.Event.SEARCH, bundle);
        }
    }

    @Override
    protected void logEvent(@NonNull final ShareEvent event) {
        final Attribute source   = event.getAttribute(ShareEvent.ATTRIBUTE_SOURCE);
        final Attribute category = event.getAttribute(ShareEvent.ATTRIBUTE_CATEGORY);

        if (source != null && category != null) {
            final Bundle bundle = new Bundle();
            bundle.putString(source.getName(), source.getValue());
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, category.getValue());

            FirebaseAnalytics.getInstance(this.context).logEvent(FirebaseAnalytics.Event.SHARE, bundle);
        }
    }

    @Override
    protected void logEvent(@NonNull final ViewEvent event) {
        final Attribute screenName = event.getAttribute(ViewEvent.ATTRIBUTE_SCREEN_NAME);
        final Attribute source     = event.getAttribute(ViewEvent.ATTRIBUTE_SOURCE);
        final Attribute category   = event.getAttribute(ViewEvent.ATTRIBUTE_CATEGORY);

        if (screenName != null && source != null && category != null) {
            final Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, screenName.getValue());
            bundle.putString(source.getName(), source.getValue());
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, category.getValue());

            FirebaseAnalytics.getInstance(this.context).logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
        }
    }

    @Override
    protected void logCustomEvent(@NonNull final Event event) {
        final Bundle bundle = new Bundle();
        for (final Attribute attribute : event.getAttributes()) bundle.putString(attribute.getName(), attribute.getValue());

        FirebaseAnalytics.getInstance(this.context).logEvent(event.getName(), bundle);
    }
}
