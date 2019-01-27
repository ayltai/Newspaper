package com.github.ayltai.newspaper.config;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import dagger.Module;
import dagger.Provides;

@Module
public final class ConfigsModule {
    public static void init(@Nonnull @NonNull @lombok.NonNull final Context context) {
        PreferenceUserConfigs.init(context);
    }

    public static void init(@Nonnull @NonNull @lombok.NonNull final Activity activity) {
        FirebaseRemoteConfigs.init(activity);
    }

    @Singleton
    @Nonnull
    @NonNull
    @Provides
    static UserConfigs provideUserConfigs() {
        return PreferenceUserConfigs.getInstance();
    }

    @Singleton
    @Nonnull
    @NonNull
    @Provides
    static RemoteConfigs provideRemoteConfigs() {
        return FirebaseRemoteConfigs.getInstance();
    }
}
