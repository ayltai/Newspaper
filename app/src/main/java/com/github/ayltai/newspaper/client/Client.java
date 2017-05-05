package com.github.ayltai.newspaper.client;

import java.io.Closeable;
import java.util.List;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.model.Item;
import com.github.ayltai.newspaper.model.Source;
import com.github.ayltai.newspaper.net.HttpClient;

import rx.Observable;

public abstract class Client implements Closeable {
    //region Variables

    protected final HttpClient client;
    protected final Source     source;

    //endregion

    //@Inject
    protected Client(@NonNull final HttpClient client, @Nullable final Source source) {
        this.client = client;
        this.source = source;
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
