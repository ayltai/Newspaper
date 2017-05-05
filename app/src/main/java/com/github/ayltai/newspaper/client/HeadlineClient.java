package com.github.ayltai.newspaper.client;

import javax.inject.Inject;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.client.rss.RssClient;
import com.github.ayltai.newspaper.model.Source;
import com.github.ayltai.newspaper.net.HttpClient;

import rx.Observable;

final class HeadlineClient extends RssClient {
    @Inject
    HeadlineClient(@NonNull final HttpClient client, @Nullable final Source source) {
        super(client, source);
    }

    @NonNull
    @Override
    public Observable<String> getFullDescription(@NonNull final String url) {
        return null;
    }
}
