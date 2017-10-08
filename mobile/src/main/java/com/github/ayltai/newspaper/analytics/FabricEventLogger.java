package com.github.ayltai.newspaper.analytics;

import javax.inject.Singleton;

import android.support.annotation.NonNull;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;
import com.github.ayltai.newspaper.util.TestUtils;

@Singleton
final class FabricEventLogger extends EventLogger {
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
        final Attribute query      = event.getAttribute(SearchEvent.ATTRIBUTE_QUERY);
        final Attribute screenName = event.getAttribute(SearchEvent.ATTRIBUTE_SCREEN_NAME);

        if (query != null && screenName != null && !TestUtils.isRunningTests()) {
            Answers.getInstance().logSearch(new com.crashlytics.android.answers.SearchEvent()
                .putQuery(query.getValue())
                .putCustomAttribute(screenName.getName(), screenName.getValue()));
        }
    }

    @Override
    protected void logEvent(@NonNull final ShareEvent event) {
        final Attribute source   = event.getAttribute(ShareEvent.ATTRIBUTE_SOURCE);
        final Attribute category = event.getAttribute(ShareEvent.ATTRIBUTE_CATEGORY);

        if (source != null && category != null && !TestUtils.isRunningTests()) {
            Answers.getInstance().logShare(new com.crashlytics.android.answers.ShareEvent()
                .putContentName(source.getValue())
                .putContentType(category.getValue()));
        }
    }

    @Override
    protected void logEvent(@NonNull final ViewEvent event) {
        final Attribute screenName = event.getAttribute(ViewEvent.ATTRIBUTE_SCREEN_NAME);
        final Attribute source     = event.getAttribute(ViewEvent.ATTRIBUTE_SOURCE);
        final Attribute category   = event.getAttribute(ViewEvent.ATTRIBUTE_CATEGORY);

        if (screenName != null && source != null && category != null && !TestUtils.isRunningTests()) {
            Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentId(screenName.getValue())
                .putContentName(source.getValue())
                .putContentType(category.getValue()));
        }
    }

    @Override
    protected void logCustomEvent(@NonNull final Event event) {
        final CustomEvent customEvent = new CustomEvent(event.getName());
        for (final Attribute attribute : event.getAttributes()) customEvent.putCustomAttribute(attribute.getName(), attribute.getValue());

        if (!TestUtils.isRunningTests()) Answers.getInstance().logCustom(customEvent);
    }
}
