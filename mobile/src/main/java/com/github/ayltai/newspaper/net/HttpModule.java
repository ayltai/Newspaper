package com.github.ayltai.newspaper.net;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.util.TestUtils;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

@Module
final class HttpModule {
    private static final int TIMEOUT_CONNECT = 10;
    private static final int TIMEOUT_READ    = 30;
    private static final int TIMEOUT_WRITE   = 30;

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
    static Retrofit provideRetrofit(@NonNull final OkHttpClient httpClient) {
        return new Retrofit.Builder()
            .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .baseUrl("http://dummy.base.url")
            .client(httpClient)
            .build();
    }

    @Singleton
    @NonNull
    @Provides
    static ApiService provideApiService(@NonNull final Retrofit retrofit) {
        return retrofit.create(ApiService.class);
    }
}
