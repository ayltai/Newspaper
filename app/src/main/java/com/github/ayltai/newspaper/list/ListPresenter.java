package com.github.ayltai.newspaper.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.github.ayltai.newspaper.BuildConfig;
import com.github.ayltai.newspaper.Configs;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.Presenter;
import com.github.ayltai.newspaper.client.Client;
import com.github.ayltai.newspaper.client.ClientFactory;
import com.github.ayltai.newspaper.data.FavoriteManager;
import com.github.ayltai.newspaper.data.ItemManager;
import com.github.ayltai.newspaper.model.Category;
import com.github.ayltai.newspaper.model.Item;
import com.github.ayltai.newspaper.model.Source;
import com.github.ayltai.newspaper.util.SuppressFBWarnings;
import com.github.ayltai.newspaper.util.TestUtils;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;

public /* final */ class ListPresenter extends Presenter<ListPresenter.View> {
    public interface View extends Presenter.View {
        void setItems(@NonNull ListScreen.Key parentKey, @NonNull List<Item> items);

        @NonNull
        Flowable<Object> refreshes();

        void showUpdateIndicator();
    }

    //region Variables

    private CompositeDisposable disposables;
    private Disposable          refreshDisposable;
    private Disposable          updateDisposable;
    private Realm               realm;
    private FavoriteManager     favoriteManager;
    private ItemManager         itemManager;
    private List<Item>          items;
    private ListScreen.Key      key;
    private boolean             isBound;

    //endregion

    @Inject
    public ListPresenter() {
    }

    public final void bind(@NonNull final Realm realm, @NonNull final ListScreen.Key key) {
        this.realm = realm;
        this.key   = key;

        if (!TestUtils.isRunningUnitTest() && this.realm.isClosed()) return;

        if (this.isViewAttached() && !this.isBound) {
            this.getItemManager().getItemsSingle(Collections.emptyList(), Collections.singletonList(this.key.getCategory()))
                .subscribe(items -> {
                    this.items = items;
                    this.getView().setItems(this.key, this.items);

                    if (!Constants.CATEGORY_BOOKMARK.equals(this.key.getCategory())) {
                        if (this.items.isEmpty()) {
                            this.bindFromRemote(Constants.REFRESH_LOAD_TIMEOUT);
                        } else {
                            this.checkForUpdate();

                            this.bindFromRemote(Constants.INIT_LOAD_TIMEOUT);
                        }
                    }

                    this.isBound = true;
                }, error -> this.log().e(this.getClass().getSimpleName(), error.getMessage(), error));
        }
    }

    @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
    private void bindFromRemote(final int timeout) {
        if (this.getView() == null) return;

        if (this.refreshDisposable != null) this.refreshDisposable.dispose();

        if (!TestUtils.isRunningUnitTest() && this.realm.isClosed()) return;

        this.getFavoriteManager().getFavorite()
            .subscribe(
                favorite -> {
                    final List<Single<List<Item>>> maybes = new ArrayList<>();

                    for (final Source source : favorite.getSources()) {
                        for (final Category category : source.getCategories()) {
                            if (ListPresenter.shouldFetch(this.key.getCategory(), category.getName())) {
                                final Client client = ClientFactory.getInstance(this.getView().getContext()).getClient(source.getName());

                                if (client != null) {
                                    maybes.add(client.getItems(category.getUrl())
                                        .timeout(timeout, TimeUnit.SECONDS)
                                        .onErrorReturnItem(Collections.emptyList())
                                        .subscribeOn(Schedulers.newThread()));
                                }
                            }
                        }
                    }

                    this.refreshDisposable = ListPresenter.zip(maybes)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            items -> {
                                if (!items.isEmpty()) {
                                    this.items = this.partialUpdate(items);

                                    this.getView().setItems(this.key, this.items);
                                }

                                this.checkForUpdate();
                            },
                            error -> {
                                this.getView().setItems(this.key, this.items);

                                if (error instanceof TimeoutException) {
                                    this.log().w(this.getClass().getSimpleName(), error.getMessage(), error);
                                } else {
                                    this.log().e(this.getClass().getSimpleName(), error.getMessage(), error);
                                }
                            }
                        );
                },
                error -> this.log().e(this.getClass().getSimpleName(), error.getMessage(), error)
        );
    }

    @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
    private void checkForUpdate() {
        if (BuildConfig.DEBUG) Log.i(this.getClass().getSimpleName(), "Scheduled update check");

        if (this.getView() == null) return;

        if (this.updateDisposable != null) this.updateDisposable.dispose();

        this.getFavoriteManager().getFavorite()
            .subscribe(
                favorite -> {
                    final List<Single<List<Item>>> singles = new ArrayList<>();

                    for (final Source source : favorite.getSources()) {
                        for (final Category category : source.getCategories()) {
                            if (ListPresenter.shouldFetch(this.key.getCategory(), category.getName())) {
                                final Client client = ClientFactory.getInstance(this.getView().getContext()).getClient(source.getName());

                                if (client != null) {
                                    singles.add(client
                                        .getItems(category.getUrl())
                                        .delaySubscription(Configs.getUpdateInterval(), TimeUnit.SECONDS)
                                        .timeout(Configs.getUpdateInterval() + Constants.REFRESH_LOAD_TIMEOUT, TimeUnit.SECONDS)
                                        .onErrorReturnItem(Collections.emptyList())
                                        .subscribeOn(Schedulers.newThread()));
                                }
                            }
                        }
                    }

                    this.updateDisposable = ListPresenter.zip(singles)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            this::showUpdateIndicator,
                            error -> {
                                if (error instanceof TimeoutException) {
                                    this.log().w(this.getClass().getSimpleName(), error.getMessage(), error);
                                } else {
                                    this.log().e(this.getClass().getSimpleName(), error.getMessage(), error);
                                }

                                this.checkForUpdate();
                            });
                },
                error -> this.log().e(this.getClass().getSimpleName(), error.getMessage(), error)
        );
    }

    private void showUpdateIndicator(@NonNull final List<Item> items) {
        if (BuildConfig.DEBUG) Log.i(this.getClass().getSimpleName(), "Update check finished");

        if (this.getView() == null) return;
        if (!TestUtils.isRunningUnitTest() && this.realm.isClosed()) return;

        if (!this.items.isEmpty() && !items.isEmpty()) {
            final Item i = this.items.get(0);
            final Item j = items.get(0);

            if (i.getPublishDate() != null && j.getPublishDate() != null && i.getPublishDate().compareTo(j.getPublishDate()) < 0) this.getView().showUpdateIndicator();

            this.checkForUpdate();
        }
    }

    @NonNull
    private List<Item> partialUpdate(@NonNull final List<Item> items) {
        if (!this.realm.isClosed()) {
            this.realm.beginTransaction();

            for (final Item item : items) {
                final Item realmItem = this.getItemManager().getItem(item.getLink());

                if (realmItem == null) {
                    realm.insert(item);
                } else {
                    if (realmItem.isFullDescription()) item.setDescription(realmItem.getDescription());
                    item.setIsFullDescription(realmItem.isFullDescription());

                    item.getImages().clear();
                    item.getImages().addAll(realmItem.getImages());

                    if (realmItem.getVideo() != null) item.setVideo(realmItem.getVideo());
                    item.setBookmarked(realmItem.isBookmarked());

                    realm.insertOrUpdate(item);
                }
            }

            this.realm.commitTransaction();
        }

        return items;
    }

    //region Lifecycle

    @Override
    public void onViewAttached(@NonNull final ListPresenter.View view) {
        super.onViewAttached(view);

        this.attachEvents();
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();

        if (this.disposables != null && !this.disposables.isDisposed() && this.disposables.size() > 0) {
            this.disposables.dispose();
            this.disposables = null;
        }

        if (this.refreshDisposable != null) {
            this.refreshDisposable.dispose();
            this.refreshDisposable = null;
        }

        if (this.updateDisposable != null) {
            this.updateDisposable.dispose();
            this.updateDisposable = null;
        }
    }

    //endregion

    @NonNull
    private FavoriteManager getFavoriteManager() {
        if (this.favoriteManager == null) this.favoriteManager = new FavoriteManager(this.getView().getContext());

        return this.favoriteManager;
    }

    @VisibleForTesting
    @NonNull
    /* private final */ ItemManager getItemManager() {
        if (this.itemManager == null) this.itemManager = new ItemManager(this.realm);

        return this.itemManager;
    }

    private void attachEvents() {
        if (this.getView() == null) return;

        if (this.disposables == null) this.disposables = new CompositeDisposable();

        this.disposables.add(this.getView().refreshes().subscribe(dummy -> {
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

    private static Single<List<Item>> zip(@NonNull final Iterable<Single<List<Item>>> singles) {
        return Single.zip(singles, lists -> {
            final List<Item> items = new ArrayList<>();

            for (final Object list : lists) items.addAll((List<Item>)list);
            Collections.sort(items);

            return items;
        });
    }

    private static boolean shouldFetch(@NonNull final String requestedCategory, @NonNull final String category) {
        return requestedCategory.equals(category) || (Constants.CATEGORY_INSTANT + requestedCategory).equals(category);
    }
}
