package com.github.ayltai.newspaper.list;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.github.ayltai.newspaper.BuildConfig;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.Presenter;
import com.github.ayltai.newspaper.data.Feed;
import com.github.ayltai.newspaper.data.FeedManager;
import com.github.ayltai.newspaper.rss.Client;
import com.github.ayltai.newspaper.rss.Item;
import com.github.ayltai.newspaper.util.SuppressFBWarnings;

import io.realm.Realm;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class ListPresenter extends Presenter<ListPresenter.View> {
    public interface View extends Presenter.View {
        void setItems(@NonNull ListScreen.Key parentKey, @NonNull Feed feed);

        @NonNull Observable<Void> refreshes();

        void showUpdateIndicator();
    }

    //region Variables

    private CompositeSubscription subscriptions;
    private Subscription          subscription;
    private Realm                 realm;
    private Client                client;
    private ListScreen.Key        key;
    private Feed                  feed;
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
                            this.bindFromRemote(Constants.REFRESH_LOAD_TIMEOUT);
                        }
                    } else {
                        this.feed = feed;

                        this.getView().setItems(this.key, feed);

                        this.checkForUpdate();

                        if (!Constants.SOURCE_BOOKMARK.equals(this.key.getUrl())) this.bindFromRemote(Constants.INIT_LOAD_TIMEOUT);
                    }

                    this.isBound = true;
                }, error -> this.log().e(this.getClass().getSimpleName(), error.getMessage(), error));
        }
    }

    @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
    private void bindFromRemote(final int timeout) {
        this.client.get(this.key.getUrl())
            .doOnNext(data -> {
                this.copyToRealmOrUpdate(data);

                this.checkForUpdate();
            })
            .timeout(timeout, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(
                feed -> {
                    this.feed = feed;

                    this.getView().setItems(this.key, feed);
                },
                error -> {
                    this.getView().setItems(this.key, this.feed);

                    if (error instanceof TimeoutException) {
                        this.log().w(this.getClass().getSimpleName(), error.getMessage(), error);
                    } else {
                        this.log().e(this.getClass().getSimpleName(), error.getMessage(), error);
                    }
                });
    }

    private void checkForUpdate() {
        if (BuildConfig.DEBUG) Log.i(this.getClass().getSimpleName(), "Scheduled update check");

        if (this.subscription != null) this.subscription.unsubscribe();

        this.subscription = this.client.get(this.key.getUrl())
            .delaySubscription(Constants.UPDATE_INTERVAL, TimeUnit.SECONDS)
            .timeout(Constants.UPDATE_INTERVAL + Constants.REFRESH_LOAD_TIMEOUT, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(this::showUpdateIndicator, error -> {
                if (error instanceof TimeoutException) {
                    this.log().w(this.getClass().getSimpleName(), error.getMessage(), error);
                } else {
                    this.log().e(this.getClass().getSimpleName(), error.getMessage(), error);
                }

                this.checkForUpdate();
            });
    }

    private void showUpdateIndicator(final Feed feed) {
        if (BuildConfig.DEBUG) Log.i(this.getClass().getSimpleName(), "Update check finished");

        if (this.feed != null && !this.feed.getItems().isEmpty() && !feed.getItems().isEmpty()) {
            final Item i = this.feed.getItems().get(0);
            final Item j = feed.getItems().get(0);

            if (i.getPublishDate() != null && j.getPublishDate() != null && i.getPublishDate().compareTo(j.getPublishDate()) < 0) this.getView().showUpdateIndicator();

            this.checkForUpdate();
        }
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

        if (this.subscription != null) {
            this.subscription.unsubscribe();
            this.subscription = null;
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
                    this.bindFromRemote(Constants.REFRESH_LOAD_TIMEOUT);
                }
            }
        }, error -> this.log().e(this.getClass().getSimpleName(), error.getMessage(), error)));
    }
}
