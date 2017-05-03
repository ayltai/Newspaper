package com.github.ayltai.newspaper.model;

import java.io.Closeable;
import java.util.List;

import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.net.HttpClient;

import rx.Observable;

public abstract class Client implements Closeable {
    protected final HttpClient client;

    //@Inject
    protected Client(@NonNull final HttpClient client) {
        this.client = client;
    }

    @NonNull
    public abstract Observable<List<Item>> getItems(@NonNull String url);

    @NonNull
    public abstract Observable<String> getFullDescription(@NonNull String url);

    @Override
    public void close() {
        this.client.close();
    }
}
