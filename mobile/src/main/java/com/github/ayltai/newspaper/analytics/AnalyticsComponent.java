package com.github.ayltai.newspaper.analytics;

import javax.inject.Singleton;

import android.support.annotation.NonNull;

import dagger.Component;

@Singleton
@Component(modules = { AnalyticsModule.class })
public interface AnalyticsComponent {
    @NonNull
    EventLogger eventLogger();
}
