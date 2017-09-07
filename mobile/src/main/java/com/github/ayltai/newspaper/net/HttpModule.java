package com.github.ayltai.newspaper.net;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import com.github.ayltai.newspaper.BuildConfig;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

@Module
public final class HttpModule {
    public static final int TIMEOUT_CONNECT = 10;
    public static final int TIMEOUT_READ    = 30;
    public static final int TIMEOUT_WRITE   = 30;

    private HttpModule() {
    }

    @Singleton
    @Provides
    static OkHttpClient.Builder provideHttpClientBuilder() {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .connectTimeout(HttpModule.TIMEOUT_CONNECT, TimeUnit.SECONDS)
            .readTimeout(HttpModule.TIMEOUT_READ, TimeUnit.SECONDS)
            .writeTimeout(HttpModule.TIMEOUT_WRITE, TimeUnit.SECONDS);

        if (BuildConfig.DEBUG) builder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));

        return builder;
    }
}
