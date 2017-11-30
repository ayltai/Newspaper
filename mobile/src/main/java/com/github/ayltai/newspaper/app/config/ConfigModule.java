package com.github.ayltai.newspaper.app.config;

import javax.inject.Singleton;

import android.app.Activity;
import android.support.annotation.NonNull;

import dagger.Module;
import dagger.Provides;

@Module
public final class ConfigModule {
    private final Activity activity;

    public ConfigModule(@NonNull final Activity activity) {
        this.activity = activity;
    }

    @Singleton
    @NonNull
    @Provides
    AppConfig provideAppConfig() {
        return new AppConfig();
    }

    @Singleton
    @NonNull
    @Provides
    UserConfig provideUserConfig() {
        return new UserConfig(this.activity, this.provideRemoteConfig());
    }

    @Singleton
    @NonNull
    @Provides
    RemoteConfig provideRemoteConfig() {
        return new RemoteConfig(this.activity);
    }
}
