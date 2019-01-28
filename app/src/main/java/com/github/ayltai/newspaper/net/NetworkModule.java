package com.github.ayltai.newspaper.net;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import androidx.annotation.NonNull;

import com.github.ayltai.newspaper.BuildConfig;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.config.DaggerConfigsComponent;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
final class NetworkModule {
    @Singleton
    @Nonnull
    @NonNull
    @Provides
    static OkHttpClient provideHttpClient() {
        return new OkHttpClient.Builder()
            .connectTimeout(DaggerConfigsComponent.builder().build().userConfigs().getConnectTimeout(), TimeUnit.SECONDS)
            .readTimeout(DaggerConfigsComponent.builder().build().userConfigs().getReadTimeout(), TimeUnit.SECONDS)
            .writeTimeout(DaggerConfigsComponent.builder().build().userConfigs().getWriteTimeout(), TimeUnit.SECONDS)
            .addInterceptor(chain -> chain.proceed(chain.request()
                .newBuilder()
                .header("User-Agent", BuildConfig.APPLICATION_ID + " " + BuildConfig.VERSION_NAME)
                .header("x-api-key", BuildConfig.API_KEY)
                .build()))
            .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build();
    }

    @Singleton
    @Nonnull
    @NonNull
    @Provides
    static Retrofit provideRetrofit(@Nonnull @NonNull @lombok.NonNull final OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
            .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .build();
    }

    @Singleton
    @Nonnull
    @NonNull
    @Provides
    static ApiService provideApiService(@Nonnull @NonNull @lombok.NonNull final Retrofit retrofit) {
        return retrofit.create(ApiService.class);
    }
}
