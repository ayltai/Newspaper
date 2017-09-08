package com.github.ayltai.newspaper.net;

import com.github.ayltai.newspaper.rss.RssFeed;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface ApiService {
    @GET
    Observable<RssFeed> getFeed(@Url String url);

    @GET
    Observable<String> getHtml(@Url String url);
}
