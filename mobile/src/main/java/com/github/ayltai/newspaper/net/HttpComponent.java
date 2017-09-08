package com.github.ayltai.newspaper.net;

import javax.inject.Singleton;

import com.github.ayltai.newspaper.client.Client;

import dagger.Component;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

@Singleton
@Component(modules = { HttpModule.class })
public interface HttpComponent {
    OkHttpClient httpClient();

    Retrofit retrofit();

    ApiService apiService();

    void inject(Client client);
}
