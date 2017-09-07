package com.github.ayltai.newspaper.net;

import javax.inject.Singleton;

import dagger.Component;
import okhttp3.OkHttpClient;

@Singleton
@Component(modules = { HttpModule.class })
public interface HttpComponent {
    OkHttpClient.Builder httpClientBuilder();
}
