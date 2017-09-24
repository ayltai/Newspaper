package com.github.ayltai.newspaper.app.screen;

import java.util.Collections;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.Log;

import com.github.ayltai.newspaper.app.view.ItemPresenter;
import com.github.ayltai.newspaper.client.Client;
import com.github.ayltai.newspaper.client.ClientFactory;
import com.github.ayltai.newspaper.data.DaggerDataComponent;
import com.github.ayltai.newspaper.data.DataManager;
import com.github.ayltai.newspaper.data.DataModule;
import com.github.ayltai.newspaper.data.ItemManager;
import com.github.ayltai.newspaper.data.model.Image;
import com.github.ayltai.newspaper.data.model.Item;
import com.github.ayltai.newspaper.data.model.NewsItem;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.TestUtils;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.realm.Realm;

public class DetailsPresenter extends ItemPresenter<DetailsPresenter.View> {
    public interface View extends ItemPresenter.View {
        @Nullable
        Flowable<Irrelevant> shareClicks();

        void share(@NonNull String url);

        void showImage(@NonNull String url);
    }

    @UiThread
    @Override
    public void bindModel(final Item model) {
        super.bindModel(model);

        if (this.getView() != null && model instanceof NewsItem && !model.isFullDescription()) {
            Single.<NewsItem>create(
                emitter -> {
                    final Client client = ClientFactory.getInstance(this.getView().getContext())
                        .getClient(model.getSource());

                    if (client == null) {
                        emitter.onError(new IllegalArgumentException("Unrecognized source " + model.getSource()));
                    } else {
                        client.updateItem((NewsItem)model)
                            .subscribe(emitter::onSuccess);
                    }
                })
                .compose(RxUtils.applySingleBackgroundSchedulers())
                .flatMap(item -> Single.<Realm>create(emitter -> emitter.onSuccess(DaggerDataComponent.builder()
                    .dataModule(new DataModule(this.getView().getContext()))
                    .build()
                    .realm()))
                    .compose(RxUtils.applySingleSchedulers(DataManager.SCHEDULER))
                    .flatMap(realm -> new ItemManager(realm).putItems(Collections.singletonList(item))
                        .compose(RxUtils.applySingleSchedulers(DataManager.SCHEDULER))))
                .compose(RxUtils.applySingleBackgroundToMainSchedulers())
                .subscribe(
                    items -> super.bindModel(items.get(0)),
                    error -> {
                        if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);
                    });
        }
    }

    @Override
    protected void onAvatarClick() {
        // TODO
    }

    @Override
    protected void onSourceClick() {
        // TODO
    }

    @Override
    protected void onBookmarkClick() {
        if (this.getModel() instanceof NewsItem) {
            ((NewsItem)this.getModel()).setBookmarked(!this.getModel().isBookmarked());

            // TODO: Updates database
        }

        if (this.getView() != null) this.getView().setIsBookmarked(this.getModel().isBookmarked());
    }

    protected void onShareClick() {
        if (this.getView() != null) this.getView().share(this.getModel().getLink());
    }

    @Override
    protected void onImageClick(@NonNull final Image image) {
        if (this.getView() != null) this.getView().showImage(image.getUrl());
    }

    @Override
    protected void onVideoClick() {
        // TODO
    }

    @CallSuper
    @Override
    public void onViewAttached(@NonNull final DetailsPresenter.View view, final boolean isFirstTimeAttachment) {
        final Flowable<Irrelevant> shareClicks = view.shareClicks();
        if (shareClicks != null) this.manageDisposable(shareClicks.subscribe(irrelevant -> this.onShareClick()));

        super.onViewAttached(view, isFirstTimeAttachment);
    }
}
