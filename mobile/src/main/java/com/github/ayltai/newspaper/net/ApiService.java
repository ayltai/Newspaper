package com.github.ayltai.newspaper.net;

import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.rss.RssFeed;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface ApiService {
    @NonNull
    @GET
    Observable<RssFeed> getFeed(@Url String url);

    @NonNull
    @GET
    Observable<String> getHtml(@Url String url);
}
