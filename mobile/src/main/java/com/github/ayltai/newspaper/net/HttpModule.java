package com.github.ayltai.newspaper.net;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.util.TestUtils;

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
    @NonNull
    @Provides
    static OkHttpClient provideHttpClient() {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .connectTimeout(HttpModule.TIMEOUT_CONNECT, TimeUnit.SECONDS)
            .readTimeout(HttpModule.TIMEOUT_READ, TimeUnit.SECONDS)
            .writeTimeout(HttpModule.TIMEOUT_WRITE, TimeUnit.SECONDS);

        if (TestUtils.isLoggable()) builder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS));

        return builder.build();
    }

    @Singleton
    @NonNull
    @Provides
    static NewsApiService provideNewsApiService() {
        return new NewsApiService.Factory().create(NewsApiService.class);
    }

    @Singleton
    @NonNull
    @Provides
    static GoogleApiService provideGoogleApiService() {
        return new GoogleApiService.Factory().create(GoogleApiService.class);
    }
}
