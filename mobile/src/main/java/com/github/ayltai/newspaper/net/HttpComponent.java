package com.github.ayltai.newspaper.net;

import javax.inject.Singleton;

import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.client.Client;

import dagger.Component;
import okhttp3.OkHttpClient;

@Singleton
@Component(modules = { HttpModule.class })
public interface HttpComponent {
    @NonNull
    OkHttpClient httpClient();

    ApiService apiService();

    void inject(Client client);
}
