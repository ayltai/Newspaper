package com.github.ayltai.newspaper.app.screen;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.app.view.ItemPresenter;
import com.github.ayltai.newspaper.data.model.Image;
import com.github.ayltai.newspaper.data.model.Item;
import com.github.ayltai.newspaper.data.model.NewsItem;
import com.github.ayltai.newspaper.util.Irrelevant;

import io.reactivex.Flowable;

public class DetailsPresenter extends ItemPresenter<DetailsPresenter.View> {
    public interface View extends ItemPresenter.View {
        @Nullable
        Flowable<Irrelevant> shareClicks();
    }

    @Override
    public void setModel(final Item model) {
        super.setModel(model);
    }

    @Override
    public void bindModel(final Item model) {
        // TODO: Gets the full news item description

        super.bindModel(model);
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
        // TODO: Shows a bottom sheet dialog
    }

    @Override
    protected void onImageClick(@NonNull final Image image) {
        // TODO
    }

    @Override
    protected void onVideoClick() {
        // TODO
    }

    @CallSuper
    @Override
    public void onViewAttached(@NonNull final DetailsPresenter.View view, final boolean isFirstTimeAttachment) {
        super.onViewAttached(view, isFirstTimeAttachment);

        final Flowable<Irrelevant> shareClicks = view.shareClicks();
        if (shareClicks != null) this.manageDisposable(shareClicks.subscribe(irrelevant -> this.onShareClick()));

        this.bindModel(this.getModel());
    }
}
