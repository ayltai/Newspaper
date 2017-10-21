package com.github.ayltai.newspaper.app.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.app.data.model.Category;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.app.data.model.SourceFactory;
import com.github.ayltai.newspaper.client.Client;
import com.github.ayltai.newspaper.client.ClientFactory;
import com.github.ayltai.newspaper.data.RealmLoader;
import com.github.ayltai.newspaper.net.NetworkUtils;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.StringUtils;
import com.github.ayltai.newspaper.util.TestUtils;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.realm.RealmObject;

public final class ItemListLoader extends RealmLoader<List<NewsItem>> {
    //region Constants

    public static final int ID = ItemListLoader.class.hashCode();

    private static final String KEY_SOURCES    = "sources";
    private static final String KEY_CATEGORIES = "categories";

    //endregion

    public static final class Builder {
        private final AppCompatActivity activity;
        private final Bundle            args = new Bundle();

        public Builder(@NonNull final AppCompatActivity activity) {
            this.activity = activity;
        }

        @NonNull
        public ItemListLoader.Builder addSource(@NonNull final String source) {
            ArrayList<String> sources = this.args.getStringArrayList(ItemListLoader.KEY_SOURCES);
            if (sources == null) sources = new ArrayList<>();

            if (!sources.contains(source)) sources.add(source);
            this.args.putStringArrayList(ItemListLoader.KEY_SOURCES, sources);

            return this;
        }

        @NonNull
        public ItemListLoader.Builder addCategory(@Nullable final String category) {
            ArrayList<String> categories = this.args.getStringArrayList(ItemListLoader.KEY_CATEGORIES);
            if (categories == null) categories = new ArrayList<>();

            if (!categories.contains(category)) categories.add(category);
            this.args.putStringArrayList(ItemListLoader.KEY_CATEGORIES, categories);

            return this;
        }

        @NonNull
        public ItemListLoader.Builder forceRefresh(final boolean refresh) {
            this.args.putBoolean(RealmLoader.KEY_REFRESH, refresh);

            return this;
        }

        @NonNull
        public Flowable<List<NewsItem>> build() {
            final ArrayList<String> categories = this.args.getStringArrayList(ItemListLoader.KEY_CATEGORIES);

            if (TestUtils.isRunningUnitTest()) return Flowable.just(Collections.emptyList());

            return Flowable.create(emitter -> this.activity
                .getSupportLoaderManager()
                .restartLoader(ItemListLoader.ID + (categories == null ? 0 : categories.toString().hashCode()), this.args, new LoaderManager.LoaderCallbacks<List<NewsItem>>() {
                    @Override
                    public Loader<List<NewsItem>> onCreateLoader(final int id, final Bundle args) {
                        return new ItemListLoader(ItemListLoader.Builder.this.activity, args);
                    }

                    @Override
                    public void onLoadFinished(final Loader<List<NewsItem>> loader, final List<NewsItem> items) {
                        emitter.onNext(items);
                    }

                    @Override
                    public void onLoaderReset(final Loader<List<NewsItem>> loader) {
                    }
                }), BackpressureStrategy.LATEST);
        }
    }

    private ItemListLoader(@NonNull final Context context, @Nullable final Bundle args) {
        super(context, args);
    }

    @NonNull
    @Override
    protected Flowable<List<NewsItem>> loadFromLocalSource(@NonNull final Context context, @Nullable final Bundle args) {
        if (!this.isValid()) return Flowable.error(new IllegalStateException("Realm instance is null"));

        return Flowable.create(emitter -> ItemManager.create(this.getRealm()).getItems(ItemListLoader.getSources(args).toArray(StringUtils.EMPTY_ARRAY), ItemListLoader.getCategories(args).toArray(StringUtils.EMPTY_ARRAY))
            .compose(RxUtils.applySingleSchedulers(this.getScheduler()))
            .map(items -> items.isEmpty() ? items : RealmObject.isManaged(items.get(0)) ? this.getRealm().copyFromRealm(items) : items)
            .map(items -> {
                Collections.sort(items);
                return items;
            })
            .subscribe(emitter::onNext), BackpressureStrategy.LATEST);
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    protected Flowable<List<NewsItem>> loadFromRemoteSource(@NonNull final Context context, @Nullable final Bundle args) {
        if (NetworkUtils.isOnline(context)) {
            final List<Single<List<NewsItem>>> singles = this.createSingles(context, args);
            if (singles.isEmpty()) Flowable.just(Collections.emptyList());

            return Flowable.create(emitter -> Single.zip(
                singles,
                lists -> {
                    final List<NewsItem> combinedList = new ArrayList<>();
                    for (final Object list : lists) combinedList.addAll((List<NewsItem>)list);

                    Collections.sort(combinedList);

                    return combinedList;
                })
                .map(items -> {
                    Collections.sort(items);
                    return items;
                })
                .flatMap(items -> {
                    if (this.isValid()) {
                        return Single.create(e -> ItemManager.create(this.getRealm())
                            .putItems(items)
                            .compose(RxUtils.applySingleSchedulers(this.getScheduler()))
                            .subscribe(
                                e::onSuccess,
                                error -> {
                                    if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);

                                    if (!e.isDisposed()) e.onError(error);
                                }));
                    } else {
                        return Single.just(items);
                    }
                })
                .subscribe(
                    emitter::onNext,
                    error -> {
                        if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);
                    }
                ), BackpressureStrategy.LATEST);
        }

        return Flowable.just(Collections.emptyList());
    }

    private List<Single<List<NewsItem>>> createSingles(@NonNull final Context context, @Nullable final Bundle args) {
        final List<Single<List<NewsItem>>> singles      = new ArrayList<>();
        final List<String>                 categories   = ItemListLoader.getCategories(args);
        final boolean                      forceRefresh = RealmLoader.isForceRefresh(args);

        for (final String source : ItemListLoader.getSources(args)) {
            for (final Category category : SourceFactory.getInstance(context).getSource(source).getCategories()) {
                if (ItemListLoader.containsCategory(categories, category)) {
                    final Client client = ClientFactory.getInstance(context).getClient(source);

                    if (client != null) singles.add(client.getItems(category.getUrl())
                        // TODO: If the previous refresh timestamp is very old, wait for a longer time to refresh
                        .timeout(forceRefresh ? Constants.REFRESH_TIMEOUT : Constants.CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                        .onErrorResumeNext(error -> {
                            if (TestUtils.isLoggable()) Log.w(this.getClass().getSimpleName(), error.getMessage(), error);

                            return Single.just(Collections.emptyList());
                        })
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(Schedulers.io()));

                    break;
                }
            }
        }

        return singles;
    }

    @NonNull
    private static List<String> getSources(@Nullable final Bundle args) {
        final List<String> sources = args == null ? Collections.emptyList() : args.getStringArrayList(ItemListLoader.KEY_SOURCES);
        return sources == null ? Collections.emptyList() : sources;
    }

    @NonNull
    private static List<String> getCategories(@Nullable final Bundle args) {
        final List<String> categories = args == null ? Collections.emptyList() : args.getStringArrayList(ItemListLoader.KEY_CATEGORIES);
        return categories == null ? Collections.emptyList() : categories;
    }

    private static boolean containsCategory(@NonNull final List<String> categories, @NonNull final Category category) {
        for (final String categoryName : categories) {
            if (category.getName().equals(categoryName)) return true;
        }

        return false;
    }
}
