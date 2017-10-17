package com.github.ayltai.newspaper.net;

import android.support.annotation.NonNull;

import com.google.gson.GsonBuilder;

import com.github.ayltai.newspaper.rss.RssFeed;

import io.reactivex.Observable;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface ApiService {
    String API_END_POINT = "";

    final class Factory {
        private final Retrofit retrofit;

        Factory() {
            this.retrofit = new Retrofit.Builder()
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                    .registerTypeAdapterFactory(AutoTypeAdapterFactory.create())
                    .create()))
                .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
                .baseUrl(ApiService.API_END_POINT)
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

    @GET
    Observable<RssFeed> getFeed(@Url String url);

    @GET
    Observable<String> getHtml(@Url String url);

    @GET
    Observable<AuthToken> getToken(@Url String url);
}
