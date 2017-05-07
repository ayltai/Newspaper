package com.github.ayltai.newspaper.client;

import javax.inject.Inject;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.client.rss.RssClient;
import com.github.ayltai.newspaper.model.Item;
import com.github.ayltai.newspaper.model.Source;
import com.github.ayltai.newspaper.net.HttpClient;

import rx.Observable;

final class OrientalDailyClient extends RssClient {
    @Inject
    OrientalDailyClient(@NonNull final HttpClient client, @Nullable final Source source) {
        super(client, source);
    }

    @NonNull
    @Override
    public Observable<Item> updateItem(@NonNull final Item item) {
        return null;
    }
}
