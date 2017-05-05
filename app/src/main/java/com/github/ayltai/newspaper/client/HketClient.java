package com.github.ayltai.newspaper.client;

import java.util.List;

import javax.inject.Inject;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.model.Item;
import com.github.ayltai.newspaper.model.Source;
import com.github.ayltai.newspaper.net.HttpClient;

import rx.Observable;

final class HketClient extends Client {
    @Inject
    HketClient(@NonNull final HttpClient client, @Nullable final Source source) {
        super(client, source);
    }

    @NonNull
    @Override
    public Observable<List<Item>> getItems(@NonNull final String url) {
        return null;
    }

    @NonNull
    @Override
    public Observable<String> getFullDescription(@NonNull final String url) {
        return null;
    }
}
