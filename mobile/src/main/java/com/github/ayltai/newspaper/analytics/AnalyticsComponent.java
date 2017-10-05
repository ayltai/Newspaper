package com.github.ayltai.newspaper.analytics;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = { AnalyticsModule.class })
public interface AnalyticsComponent {
    EventLogger eventLogger();
}
