package com.github.ayltai.newspaper.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.github.ayltai.newspaper.data.model.Item;
import com.github.ayltai.newspaper.data.model.Source;
import com.github.ayltai.newspaper.net.ApiService;
import com.github.ayltai.newspaper.rss.RssFeed;
import com.github.ayltai.newspaper.rss.RssItem;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.TestUtils;

import io.reactivex.Single;
import okhttp3.OkHttpClient;

public abstract class RssClient extends Client {
    protected RssClient(@NonNull final OkHttpClient client, @NonNull final ApiService apiService, @NonNull final Source source) {
        super(client, apiService, source);
    }

    @WorkerThread
    @NonNull
    @Override
    public final Single<List<Item>> getItems(@NonNull final String url) {
        final String category = this.getCategoryName(url);

        return Single.create(emitter -> this.apiService
            .getFeed(url)
            .compose(RxUtils.applyObservableBackgroundSchedulers())
            .map(feed -> this.filter(url, feed))
            .subscribe(
                items -> {
                    for (final Item item : items) {
                        item.setSource(this.source.getName());
                        if (category != null) item.setCategory(category);
                    }

                    Collections.sort(items);

                    emitter.onSuccess(items);
                },
                error -> {
                    if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);

                    emitter.onError(error);
                }
            ));
    }

    @NonNull
    protected List<Item> filter(@NonNull final String url, @NonNull final RssFeed feed) {
        final String     category = this.getCategoryName(url);
        final List<Item> items    = new ArrayList<>();

        if (feed.getItems() != null) {
            for (final RssItem item : feed.getItems()) items.add(new Item(item, this.source.getName(), category));
        }

        return items;
    }
}
