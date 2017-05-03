package com.github.ayltai.newspaper.list;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.github.ayltai.newspaper.BuildConfig;
import com.github.ayltai.newspaper.Configs;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.Presenter;
import com.github.ayltai.newspaper.data.ItemManager;
import com.github.ayltai.newspaper.model.Client;
import com.github.ayltai.newspaper.model.Item;
import com.github.ayltai.newspaper.util.SuppressFBWarnings;
import com.github.ayltai.newspaper.util.TestUtils;

import io.realm.Realm;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public /* final */ class ListPresenter extends Presenter<ListPresenter.View> {
    public interface View extends Presenter.View {
        void setItems(@NonNull ListScreen.Key parentKey, @Nullable List<Item> items);

        @NonNull
        Observable<Void> refreshes();

        void showUpdateIndicator();
    }

    //region Variables

    @Inject
    Client client;

    private CompositeSubscription subscriptions;
    private Subscription          refreshSubscription;
    private Subscription          updateSubscription;
    private Realm                 realm;
    private ListScreen.Key        key;
    private List<Item>            items;
    private boolean               isBound;

    //endregion

    @Inject
    public ListPresenter() {
    }

    public final void bind(@NonNull final Realm realm, @NonNull final ListScreen.Key key) {
        this.realm = realm;
        this.key   = key;

        if (!TestUtils.isRunningUnitTest() && this.realm.isClosed()) return;

        if (this.isViewAttached() && !this.isBound) {
            this.getItemManager().getItems(null, new String[] { this.key.getCategory() })
                .subscribe(items -> {
                    this.items = items;

                    if (this.items.isEmpty()) {
                        if (Constants.CATEGORY_BOOKMARK.equals(this.key.getCategory())) {
                            this.getView().setItems(this.key, this.items);
                        } else {
                            this.bindFromRemote(Constants.REFRESH_LOAD_TIMEOUT);
                        }
                    } else {
                        this.checkForUpdate();

                        if (!Constants.CATEGORY_BOOKMARK.equals(this.key.getCategory())) this.bindFromRemote(Constants.INIT_LOAD_TIMEOUT);
                    }

                    this.isBound = true;
                }, error -> this.log().e(this.getClass().getSimpleName(), error.getMessage(), error));
        }
    }

    @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
    private void bindFromRemote(final int timeout) {
        if (this.refreshSubscription != null) this.refreshSubscription.unsubscribe();

        if (!TestUtils.isRunningUnitTest() && this.realm.isClosed()) return;

        // TODO: Maps category to URL
        this.refreshSubscription = this.client.getItems(this.key.getCategory())
            .doOnNext(items -> {
                this.copyToRealmOrUpdate(items);

                this.checkForUpdate();
            })
            .timeout(timeout, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(
                items -> {
                    this.items = items;

                    this.getView().setItems(this.key, items);
                },
                error -> {
                    this.getView().setItems(this.key, this.items);

                    if (error instanceof TimeoutException) {
                        this.log().w(this.getClass().getSimpleName(), error.getMessage(), error);
                    } else {
                        this.log().e(this.getClass().getSimpleName(), error.getMessage(), error);
                    }
                });
    }

    @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
    private void checkForUpdate() {
        if (BuildConfig.DEBUG) Log.i(this.getClass().getSimpleName(), "Scheduled update check");

        if (this.updateSubscription != null) this.updateSubscription.unsubscribe();

        // TODO: Maps category to URL
        this.updateSubscription = this.client.getItems(this.key.getCategory())
            .delaySubscription(Configs.getUpdateInterval(), TimeUnit.SECONDS)
            .timeout(Configs.getUpdateInterval() + Constants.REFRESH_LOAD_TIMEOUT, TimeUnit.SECONDS)
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

    private void showUpdateIndicator(final List<Item> items) {
        if (BuildConfig.DEBUG) Log.i(this.getClass().getSimpleName(), "Update check finished");

        if (!TestUtils.isRunningUnitTest() && this.realm.isClosed()) return;

        if (!this.items.isEmpty() && !items.isEmpty()) {
            final Item i = this.items.get(0);
            final Item j = items.get(0);

            if (i.getPublishDate() != null && j.getPublishDate() != null && i.getPublishDate().compareTo(j.getPublishDate()) < 0) this.getView().showUpdateIndicator();

            this.checkForUpdate();
        }
    }

    private void copyToRealmOrUpdate(@NonNull final List<Item> items) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (!this.realm.isClosed()) {
                this.realm.beginTransaction();
                this.realm.copyToRealmOrUpdate(items);
                this.realm.commitTransaction();
            }
        });
    }

    //region Lifecycle

    @Override
    public void onViewAttached(@NonNull final ListPresenter.View view) {
        super.onViewAttached(view);

        // TODO: Creates client of selected sources
        if (this.client == null) this.client = null;

        this.attachEvents();
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();

        if (this.subscriptions != null && this.subscriptions.hasSubscriptions()) {
            this.subscriptions.unsubscribe();
            this.subscriptions = null;
        }

        if (this.refreshSubscription != null) {
            this.refreshSubscription.unsubscribe();
            this.refreshSubscription = null;
        }

        if (this.updateSubscription != null) {
            this.updateSubscription.unsubscribe();
            this.updateSubscription = null;
        }

        if (this.client != null) {
            this.client.close();
            this.client = null;
        }
    }

    //endregion

    @VisibleForTesting
    @NonNull
    /* private final */ ItemManager getItemManager() {
        return new ItemManager(this.realm);
    }

    private void attachEvents() {
        if (this.subscriptions == null) this.subscriptions = new CompositeSubscription();

        this.subscriptions.add(this.getView().refreshes().subscribe(dummy -> {
            if (this.key != null) {
                if (Constants.CATEGORY_BOOKMARK.equals(this.key.getCategory())) {
                    this.isBound = false;

                    this.bind(this.realm, this.key);
                } else {
                    this.bindFromRemote(Constants.REFRESH_LOAD_TIMEOUT);
                }
            }
        }, error -> this.log().e(this.getClass().getSimpleName(), error.getMessage(), error)));
    }
}
