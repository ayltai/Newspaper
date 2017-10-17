package com.github.ayltai.newspaper.net;

import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.rss.RssFeed;

import io.reactivex.Observable;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface NewsApiService {
    final class Factory {
        private final Retrofit retrofit;

        Factory() {
            this.retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .baseUrl("http://dummy.base.url")
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
}
