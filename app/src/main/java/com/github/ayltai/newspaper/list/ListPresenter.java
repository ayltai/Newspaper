package com.github.ayltai.newspaper.list;

import java.util.concurrent.TimeUnit;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.Presenter;
import com.github.ayltai.newspaper.data.Feed;
import com.github.ayltai.newspaper.data.FeedManager;
import com.github.ayltai.newspaper.rss.Client;
import com.github.ayltai.newspaper.util.LogUtils;

import io.realm.Realm;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class ListPresenter extends Presenter<ListPresenter.View> {
    public interface View extends Presenter.View {
        void setItems(@NonNull ListScreen.Key parentKey, @NonNull Feed feed);

        @NonNull Observable<Void> refreshes();
    }

    //region Variables

    private CompositeSubscription subscriptions;
    private Realm                 realm;
    private Client                client;
    private ListScreen.Key        key;
    private boolean               isBound;

    //endregion

    public ListPresenter() {
    }

    public final void bind(@NonNull final Realm realm, @NonNull final ListScreen.Key key) {
        this.realm = realm;
        this.key   = key;

        if (this.isViewAttached() && !this.isBound) {
            this.getFeedManager().getFeed(this.key.getUrl())
                .subscribe(feed -> {
                    if (feed == null || feed.getItems().isEmpty()) {
                        if (feed != null && Constants.SOURCE_BOOKMARK.equals(this.key.getUrl())) {
                            this.getView().setItems(this.key, feed);
                        } else {
                            this.bindFromRemote();
                        }
                    } else {
                        this.getView().setItems(this.key, feed);

                        if (!Constants.SOURCE_BOOKMARK.equals(this.key.getUrl())) {
                            this.bindFromRemote(Constants.INIT_LOAD_TIMEOUT);
                        }
                    }

                    this.isBound = true;
                }, error -> FirebaseCrash.logcat(Log.ERROR, this.getClass().getName(), error.getMessage()));
        }
    }

    private void bindFromRemote() {
        this.client.get(this.key.getUrl())
            .doOnNext(this::copyToRealmOrUpdate)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(data -> this.getView().setItems(this.key, data), error -> LogUtils.e(this.getClass().getName(), error.getMessage(), error));
    }

    private void bindFromRemote(final int timeout) {
        this.client.get(this.key.getUrl())
            .doOnNext(this::copyToRealmOrUpdate)
            .timeout(timeout, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(data -> this.getView().setItems(this.key, data), error -> LogUtils.e(this.getClass().getName(), error.getMessage(), error));
    }

    private void copyToRealmOrUpdate(@NonNull final Feed feed) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (!this.realm.isClosed()) {
                this.realm.beginTransaction();
                this.realm.copyToRealmOrUpdate(feed);
                this.realm.commitTransaction();
            }
        });

    }

    //region Lifecycle

    @Override
    public void onViewAttached(@NonNull final ListPresenter.View view) {
        super.onViewAttached(view);

        this.attachEvents();

        if (this.client == null) this.client = new Client();
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();

        if (this.subscriptions != null && this.subscriptions.hasSubscriptions()) {
            this.subscriptions.unsubscribe();
            this.subscriptions = null;
        }

        if (this.client != null) this.client.close();
    }

    //endregion

    @VisibleForTesting
    FeedManager getFeedManager() {
        return new FeedManager(this.realm);
    }

    private void attachEvents() {
        if (this.subscriptions == null) this.subscriptions = new CompositeSubscription();

        this.subscriptions.add(this.getView().refreshes().subscribe(dummy -> {
            if (this.key != null) {
                if (Constants.SOURCE_BOOKMARK.equals(this.key.getUrl())) {
                    this.isBound = false;

                    this.bind(this.realm, this.key);
                } else {
                    this.bindFromRemote();
                }
            }
        }, error -> LogUtils.e(this.getClass().getName(), error.getMessage(), error)));
    }
}
