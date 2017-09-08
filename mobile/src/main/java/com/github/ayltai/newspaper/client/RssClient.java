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
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.TestUtils;

import io.reactivex.Single;
import okhttp3.OkHttpClient;

public abstract class RssClient extends Client {
    protected final ApiService apiService;

    protected RssClient(@NonNull final OkHttpClient client, @NonNull final Source source, @NonNull final ApiService apiService) {
        super(client, source);

        this.apiService = apiService;
    }

    @WorkerThread
    @NonNull
    @Override
    public final Single<List<Item>> getItems(@NonNull final String url) {
        final String category = this.getCategoryName(url);

        return Single.create(emitter -> {
            if (this.source == null) {
                emitter.onSuccess(Collections.emptyList());
            } else {
                this.apiService
                    .getFeed(url)
                    .compose(RxUtils.applyObservableBackgroundSchedulers())
                    .map(feed -> this.filter(url, feed))
                    .subscribe(
                        items -> {
                            for (final Item item : items) {
                                item.setSource(this.source.getName());
                                item.setCategory(category);
                            }

                            Collections.sort(items);

                            emitter.onSuccess(items);
                        },
                        error -> {
                            if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);

                            emitter.onError(error);
                        }
                    );
            }
        });
    }

    @NonNull
    protected List<Item> filter(@NonNull final String url, @NonNull final RssFeed feed) {
        final String     category = this.getCategoryName(url);
        final List<Item> items    = new ArrayList<>();

        if (feed.getItems() != null) {
            for (int i = 0; i < feed.getItems().size(); i++) items.add(new Item(feed.getItems().get(i), this.source.getName(), category));
        }

        return items;
    }
}
