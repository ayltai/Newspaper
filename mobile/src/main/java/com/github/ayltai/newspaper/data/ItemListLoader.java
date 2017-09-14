package com.github.ayltai.newspaper.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.github.ayltai.newspaper.client.Client;
import com.github.ayltai.newspaper.client.ClientFactory;
import com.github.ayltai.newspaper.data.model.Category;
import com.github.ayltai.newspaper.data.model.Item;
import com.github.ayltai.newspaper.data.model.SourceFactory;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.TestUtils;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;

public final class ItemListLoader extends RealmLoader<List<Item>> {
    //region Constants

    public static final int ID = ItemListLoader.class.hashCode();

    private static final String KEY_SOURCE   = "source";
    private static final String KEY_CATEGORY = "category";

    //endregion

    public static final class Builder {
        private final AppCompatActivity activity;
        private final Bundle            args = new Bundle();

        public Builder(@NonNull final AppCompatActivity activity) {
            this.activity = activity;
        }

        @NonNull
        public ItemListLoader.Builder addSource(@NonNull final String source) {
            ArrayList<String> sources = this.args.getStringArrayList(ItemListLoader.KEY_SOURCE);
            if (sources == null) sources = new ArrayList<>();

            if (!sources.contains(source)) sources.add(source);
            this.args.putStringArrayList(ItemListLoader.KEY_SOURCE, sources);

            return this;
        }

        @NonNull
        public ItemListLoader.Builder setCategory(@Nullable final String category) {
            if (TextUtils.isEmpty(category)) {
                this.args.remove(ItemListLoader.KEY_CATEGORY);
            } else {
                this.args.putString(ItemListLoader.KEY_CATEGORY, category);
            }

            return this;
        }

        @NonNull
        public Flowable<List<Item>> build() {
            return Flowable.create(emitter -> this.activity
                .getSupportLoaderManager()
                .restartLoader(ItemListLoader.ID, this.args, new LoaderManager.LoaderCallbacks<List<Item>>() {
                    @Override
                    public Loader<List<Item>> onCreateLoader(final int id, final Bundle args) {
                        return new ItemListLoader(ItemListLoader.Builder.this.activity, args);
                    }

                    @Override
                    public void onLoadFinished(final Loader<List<Item>> loader, final List<Item> items) {
                        emitter.onNext(items);
                    }

                    @Override
                    public void onLoaderReset(final Loader<List<Item>> loader) {
                    }
                }), BackpressureStrategy.LATEST);
        }
    }

    private ItemListLoader(@NonNull final Context context, @Nullable final Bundle args) {
        super(context, args);
    }

    @NonNull
    @Override
    protected Observable<List<Item>> loadFromLocalSource(@NonNull final Context context, @Nullable final Bundle args) {
        if (this.getRealm() == null) return Observable.error(new IllegalStateException("Realm instance is null"));

        final String category = ItemListLoader.getCategory(args);

        return Observable.create(emitter -> new ItemManager(this.getRealm()).getItems(ItemListLoader.getSources(args).toArray(new String[0]), category)
            .compose(RxUtils.applySingleSchedulers(this.getScheduler()))
            .map(items -> this.getRealm().copyFromRealm(items))
            .subscribe(emitter::onNext));
    }

    @NonNull
    @Override
    protected Observable<List<Item>> loadFromRemoteSource(@NonNull final Context context, @Nullable final Bundle args) {
        return Observable.create(emitter -> {
            final List<Single<List<Item>>> singles      = new ArrayList<>();
            final String                   categoryName = ItemListLoader.getCategory(args);

            for (final String source : ItemListLoader.getSources(args)) {
                for (final Category category : SourceFactory.getInstance(context).getSource(source).getCategories()) {
                    if (category.getName().equals(categoryName)) {
                        final Client client = ClientFactory.getInstance(context).getClient(source);
                        if (client != null) singles.add(client.getItems(category.getUrl()));
                    }
                }
            }

            Single.merge(singles)
                .compose(RxUtils.applyFlowableBackgroundSchedulers())
                .doOnNext(items -> {
                    if (this.getRealm() != null) new ItemManager(this.getRealm()).putItems(items);
                })
                .subscribe(
                    emitter::onNext,
                    error -> {
                        if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);
                    }
                );
        });
    }

    @NonNull
    private static List<String> getSources(@Nullable final Bundle args) {
        final List<String> sources = args == null ? Collections.emptyList() : args.getStringArrayList(ItemListLoader.KEY_SOURCE);
        return sources == null ? Collections.emptyList() : sources;
    }

    @Nullable
    private static String getCategory(@Nullable final Bundle args) {
        return args == null ? null : args.getString(ItemListLoader.KEY_CATEGORY);
    }
}
