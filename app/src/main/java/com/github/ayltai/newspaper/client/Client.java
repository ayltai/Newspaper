package com.github.ayltai.newspaper.client;

import java.io.Closeable;
import java.util.List;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.model.Category;
import com.github.ayltai.newspaper.model.Item;
import com.github.ayltai.newspaper.model.Source;
import com.github.ayltai.newspaper.net.HttpClient;

import rx.Observable;

public abstract class Client implements Closeable {
    protected static final String ENCODING = "UTF-8";

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
    public abstract Observable<Item> updateItem(@NonNull Item item);

    @Override
    public void close() {
        this.client.close();
    }

    @Nullable
    protected final String getCategoryName(@NonNull final String url) {
        for (final Category category : this.source.getCategories()) {
            if (category.getUrl().equals(url)) return category.getName();
        }

        return null;
    }
}
