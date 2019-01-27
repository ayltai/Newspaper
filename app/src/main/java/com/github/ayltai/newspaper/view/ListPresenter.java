package com.github.ayltai.newspaper.view;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import android.content.Context;
import android.util.Log;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.akaita.java.rxjava2debug.RxJava2Debug;
import com.github.ayltai.newspaper.data.DataManager;
import com.github.ayltai.newspaper.data.ItemManager;
import com.github.ayltai.newspaper.data.model.Item;
import com.github.ayltai.newspaper.net.DaggerNetworkComponent;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.util.NetworkUtils;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.StringUtils;

import io.reactivex.Flowable;
import io.reactivex.Single;

public final class ListPresenter extends ModelPresenter<List<Item>, ListPresenter.View> {
    public interface View extends Presenter.View {
        @Nonnull
        @NonNull
        Flowable<Irrelevant> refreshActions();

        @Nonnull
        @NonNull
        Flowable<Irrelevant> clearActions();

        void bind(@Nonnull @NonNull @lombok.NonNull List<Item> items);

        void refresh();

        void clear();

        void showEmptyView();

        void hideEmptyView();

        void showLoadingView();

        void hideLoadingView();
    }

    private static final String COMMA = ",";

    private final List<String> sourceNames;
    private final String       categoryName;

    public ListPresenter(@Nonnull @NonNull @lombok.NonNull final List<String> sourceNames, @Nonnull @NonNull @lombok.NonNull final String categoryName) {
        this.sourceNames  = sourceNames;
        this.categoryName = categoryName;
    }

    @Override
    public void bindModel() {
        if (this.getView() != null) {
            if (this.getModel() != null) {
                if (this.getModel().isEmpty()) {
                    this.getView().showEmptyView();
                } else {
                    this.getView().bind(this.getModel());
                    this.getView().hideEmptyView();
                }
            }

            this.getView().hideLoadingView();
        }
    }

    @CallSuper
    @Override
    public void onViewAttached(@Nonnull @NonNull @lombok.NonNull final View view, final boolean isFirstAttachment) {
        super.onViewAttached(view, isFirstAttachment);

        if (isFirstAttachment) {
            view.showLoadingView();

            this.manageDisposable(this.load()
                .compose(RxUtils.applySingleBackgroundToMainSchedulers())
                .subscribe(
                    items -> {
                        this.setModel(items);
                        this.bindModel();
                    },
                    error -> {
                        if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));
                    }
                ));
        }

        this.subscribeRefreshActions(view);
        this.subscribeClearActions(view);
    }

    @Nonnull
    @NonNull
    protected Single<List<Item>> load() {
        if (this.getView() == null) return Single.just(Collections.emptyList());

        if (NetworkUtils.isOnline(this.getView().getContext())) return this.loadFromInternet();

        return this.loadFromDatabase(this.getView().getContext());
    }

    @Nonnull
    @NonNull
    private Single<List<Item>> loadFromDatabase(@Nonnull @NonNull @lombok.NonNull final Context context) {
        return ItemManager.create(context)
            .flatMap(manager -> manager.get(this.sourceNames, Collections.singletonList(this.categoryName), null))
            .compose(RxUtils.applySingleSchedulers(DataManager.SCHEDULER));
    }

    @Nonnull
    @NonNull
    private Single<List<Item>> loadFromInternet() {
        return DaggerNetworkComponent.create()
            .apiService()
            .query(StringUtils.join(this.sourceNames, ListPresenter.COMMA), this.categoryName)
            .compose(RxUtils.applySingleBackgroundSchedulers());
    }

    @Nonnull
    @NonNull
    protected Single<Irrelevant> clear() {
        return Single.just(Irrelevant.INSTANCE);
    }

    private void subscribeRefreshActions(@Nonnull @NonNull @lombok.NonNull final ListPresenter.View view) {
        this.manageDisposable(view.refreshActions()
            .flatMap(irrelevant -> this.load().toFlowable())
            .compose(RxUtils.applyFlowableBackgroundToMainSchedulers())
            .subscribe(
                items -> {
                    this.setModel(items);
                    this.bindModel();
                },
                error -> {
                    if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));
                }
            ));
    }

    private void subscribeClearActions(@Nonnull @NonNull @lombok.NonNull final ListPresenter.View view) {
        this.manageDisposable(view.clearActions()
            .flatMap(irrelevant -> this.clear().toFlowable())
            .compose(RxUtils.applyFlowableBackgroundToMainSchedulers())
            .subscribe(
                irrelevant -> {
                    this.setModel(Collections.emptyList());
                    this.bindModel();
                },
                error -> {
                    if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));
                }
            ));
    }
}
