package com.github.ayltai.newspaper.config;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import androidx.annotation.NonNull;

import dagger.Component;

@Singleton
@Component(modules = { ConfigsModule.class })
public interface ConfigsComponent {
    @Nonnull
    @NonNull
    UserConfigs userConfigs();

    @Nonnull
    @NonNull
    RemoteConfigs remoteConfigs();
}
