package com.github.ayltai.newspaper.app.config;

import javax.inject.Singleton;

import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.app.MainActivity;

import dagger.Component;

@Singleton
@Component(modules = { ConfigModule.class })
public interface ConfigComponent {
    @NonNull
    AppConfig appConfig();

    @NonNull
    UserConfig userConfig();

    @NonNull
    RemoteConfig remoteConfig();

    void inject(@NonNull MainActivity activity);
}
