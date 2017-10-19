package com.github.ayltai.newspaper.net;

import android.support.annotation.NonNull;

import com.google.gson.GsonBuilder;

import com.github.ayltai.newspaper.language.AuthToken;

import io.reactivex.Observable;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;

public interface GoogleApiService {
    final class Factory {
        private final Retrofit retrofit;

        Factory() {
            this.retrofit = new Retrofit.Builder()
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                    .registerTypeAdapterFactory(AutoTypeAdapterFactory.create())
                    .create()))
                .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
                .baseUrl("https://us-central1-newspaper-84169.cloudfunctions.net/")
                .client(DaggerHttpComponent.builder()
                    .build()
                    .httpClient())
                .build();
        }

        @NonNull
        public <T> T create(@NonNull final Class<T> clazz) {
            return this.retrofit.create(clazz);
        }
    }

    @GET("getToken")
    Observable<AuthToken> getToken();
}
