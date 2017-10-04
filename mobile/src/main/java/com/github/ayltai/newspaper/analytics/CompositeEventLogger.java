package com.github.ayltai.newspaper.analytics;

import java.util.List;

import javax.inject.Singleton;

import android.support.annotation.NonNull;

@Singleton
final class CompositeEventLogger extends EventLogger {
    static final class Builder {
        private List<EventLogger> eventLoggers;

        @NonNull
        public CompositeEventLogger.Builder add(@NonNull final EventLogger eventLogger) {
            this.eventLoggers.add(eventLogger);
            return this;
        }

        @NonNull
        public EventLogger build() {
            return new CompositeEventLogger(this.eventLoggers.toArray(new EventLogger[this.eventLoggers.size()]));
        }
    }

    private final EventLogger[] eventLoggers;

    private CompositeEventLogger(@NonNull final EventLogger... eventLoggers) {
        this.eventLoggers = eventLoggers;
    }

    @Override
    protected void logEvent(@NonNull final AppOpenEvent event) {
        for (final EventLogger eventLogger : this.eventLoggers) eventLogger.logEvent(event);
    }

    @Override
    protected void logEvent(@NonNull final ClickEvent event) {
        for (final EventLogger eventLogger : this.eventLoggers) eventLogger.logEvent(event);
    }

    @Override
    protected void logEvent(@NonNull final CountEvent event) {
        for (final EventLogger eventLogger : this.eventLoggers) eventLogger.logEvent(event);
    }

    @Override
    protected void logEvent(@NonNull final SearchEvent event) {
        for (final EventLogger eventLogger : this.eventLoggers) eventLogger.logEvent(event);
    }

    @Override
    protected void logEvent(@NonNull final ShareEvent event) {
        for (final EventLogger eventLogger : this.eventLoggers) eventLogger.logEvent(event);
    }

    @Override
    protected void logEvent(@NonNull final ViewEvent event) {
        for (final EventLogger eventLogger : this.eventLoggers) eventLogger.logEvent(event);
    }

    @Override
    protected void logCustomEvent(@NonNull final Event event) {
        for (final EventLogger eventLogger : this.eventLoggers) eventLogger.logCustomEvent(event);
    }
}
