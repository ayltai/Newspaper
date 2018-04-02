package com.github.ayltai.newspaper.net;

import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.rss.RssFeed;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface ApiService {
    @NonNull
    @GET
    Observable<RssFeed> getFeed(@Url String url);

    @NonNull
    @GET
    Observable<String> getHtml(@Url String url);

    @NonNull
    @FormUrlEncoded
    @POST
    Observable<String> postHtml(@Url String url, @Field("sid") int sectionId, @Field("p") int page);
}
