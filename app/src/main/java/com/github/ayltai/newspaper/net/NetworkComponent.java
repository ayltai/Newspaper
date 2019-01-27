package com.github.ayltai.newspaper.net;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import androidx.annotation.NonNull;

import dagger.Component;
import okhttp3.OkHttpClient;

@Singleton
@Component(modules = { NetworkModule.class })
public interface NetworkComponent {
    @Nonnull
    @NonNull
    OkHttpClient okHttpClient();

    @Nonnull
    @NonNull
    ApiService apiService();
}
