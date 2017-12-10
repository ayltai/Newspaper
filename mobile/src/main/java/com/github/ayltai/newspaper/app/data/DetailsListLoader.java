package com.github.ayltai.newspaper.app.data;

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

import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.data.RealmLoader;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.StringUtils;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.realm.RealmObject;

public final class DetailsListLoader extends RealmLoader<List<NewsItem>> {
    //region Constants

    public static final int ID = DetailsListLoader.class.hashCode();

    private static final String KEY_SOURCES    = "sources";
    private static final String KEY_CATEGORIES = "categories";
    private static final String KEY_POSITION   = "position";

    //endregion

    public static final class Builder {
        private final AppCompatActivity activity;
        private final Bundle            args = new Bundle();

        public Builder(@NonNull final AppCompatActivity activity) {
            this.activity = activity;
        }

        @NonNull
        public DetailsListLoader.Builder addSource(@NonNull final String source) {
            ArrayList<String> sources = this.args.getStringArrayList(DetailsListLoader.KEY_SOURCES);
            if (sources == null) sources = new ArrayList<>();

            if (!sources.contains(source)) sources.add(source);
            this.args.putStringArrayList(DetailsListLoader.KEY_SOURCES, sources);

            return this;
        }

        @NonNull
        public DetailsListLoader.Builder addCategory(@Nullable final String category) {
            ArrayList<String> categories = this.args.getStringArrayList(DetailsListLoader.KEY_CATEGORIES);
            if (categories == null) categories = new ArrayList<>();

            if (!categories.contains(category)) categories.add(category);
            this.args.putStringArrayList(DetailsListLoader.KEY_CATEGORIES, categories);

            return this;
        }

        @NonNull
        public DetailsListLoader.Builder setPosition(final int position) {
            this.args.putInt(DetailsListLoader.KEY_POSITION, position);

            return this;
        }

        @NonNull
        public Flowable<List<NewsItem>> build() {
            if (DevUtils.isRunningUnitTest()) return Flowable.just(Collections.emptyList());

            final ArrayList<String> categories = this.args.getStringArrayList(DetailsListLoader.KEY_CATEGORIES);
            final int               position   = this.args.getInt(DetailsListLoader.KEY_POSITION, 0);

            return Flowable.create(emitter -> this.activity
                .getSupportLoaderManager()
                .restartLoader(DetailsListLoader.ID + (categories == null ? 0 : categories.toString().hashCode()) + position, this.args, new LoaderManager.LoaderCallbacks<List<NewsItem>>() {
                    @NonNull
                    @Override
                    public Loader<List<NewsItem>> onCreateLoader(final int id, final Bundle args) {
                        return new DetailsListLoader(DetailsListLoader.Builder.this.activity, args);
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

    private DetailsListLoader(@NonNull final Context context, @Nullable final Bundle args) {
        super(context, args);
    }

    @NonNull
    @Override
    protected Flowable<List<NewsItem>> loadFromLocalSource(@NonNull final Context context, @Nullable final Bundle args) {
        if (!this.isValid()) return Flowable.error(new IllegalStateException("Realm instance is null"));

        return Flowable.create(emitter -> ItemManager.create(this.getRealm()).getItems(DetailsListLoader.getSources(args).toArray(StringUtils.EMPTY_ARRAY), DetailsListLoader.getCategories(args).toArray(StringUtils.EMPTY_ARRAY))
            .compose(RxUtils.applySingleSchedulers(this.getScheduler()))
            .map(items -> items.isEmpty() ? items : RealmObject.isManaged(items.get(0)) ? this.getRealm().copyFromRealm(items) : items)
            .map(items -> {
                Collections.sort(items);
                return items;
            })
            .map(items -> items.isEmpty() ? items : items.subList(DetailsListLoader.getPosition(args), items.size() - 1))
            .subscribe(emitter::onNext), BackpressureStrategy.LATEST);
    }

    @NonNull
    @Override
    protected Flowable<List<NewsItem>> loadFromRemoteSource(@NonNull final Context context, @Nullable final Bundle args) {
        return Flowable.just(Collections.emptyList());
    }

    @NonNull
    private static List<String> getSources(@Nullable final Bundle args) {
        final List<String> sources = args == null ? Collections.emptyList() : args.getStringArrayList(DetailsListLoader.KEY_SOURCES);
        return sources == null ? Collections.emptyList() : sources;
    }

    @NonNull
    private static List<String> getCategories(@Nullable final Bundle args) {
        final List<String> categories = args == null ? Collections.emptyList() : args.getStringArrayList(DetailsListLoader.KEY_CATEGORIES);
        return categories == null ? Collections.emptyList() : categories;
    }

    private static int getPosition(@Nullable final Bundle args) {
        return args == null ? 0 : args.getInt(DetailsListLoader.KEY_POSITION, 0);
    }
}
