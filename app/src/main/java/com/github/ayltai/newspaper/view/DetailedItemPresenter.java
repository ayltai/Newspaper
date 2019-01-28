package com.github.ayltai.newspaper.view;

import javax.annotation.Nonnull;

import android.content.Context;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.ayltai.newspaper.data.model.Image;
import com.github.ayltai.newspaper.data.model.Video;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.widget.DetailedItemView;

import io.reactivex.Flowable;

public final class DetailedItemPresenter extends ItemPresenter<DetailedItemPresenter.View> {
    public interface View extends ItemPresenter.View {
        @Nullable
        Flowable<Irrelevant> textToSpeechClicks();

        @Nullable
        Flowable<Irrelevant> viewOnWebClicks();

        @Nullable
        Flowable<Irrelevant> shareClicks();

        void textToSpeech();

        void viewOnWeb(@Nonnull @NonNull @lombok.NonNull String url);

        void share(@Nonnull @NonNull @lombok.NonNull String url);

        void showImage(@Nonnull @NonNull @lombok.NonNull String url);
    }

    public static final class Factory implements Presenter.Factory<DetailedItemPresenter, DetailedItemPresenter.View> {
        @Override
        public boolean isSupported(@Nonnull @NonNull @lombok.NonNull final Object key) {
            return key instanceof DetailedItemView.Key;
        }

        @NonNull
        @Nonnull
        @Override
        public DetailedItemPresenter createPresenter() {
            return new DetailedItemPresenter();
        }

        @NonNull
        @Nonnull
        @Override
        public DetailedItemPresenter.View createView(@Nonnull @NonNull @lombok.NonNull final Context context) {
            return new DetailedItemView(context);
        }
    }

    @CallSuper
    @Override
    public void onViewAttached(@Nonnull @NonNull @lombok.NonNull final View view, final boolean isFirstTimeAttachment) {
        if (view.textToSpeechClicks() != null) this.manageDisposable(view.textToSpeechClicks().subscribe(irrelevant -> this.onTextToSpeechClick()));
        if (view.viewOnWebClicks() != null) this.manageDisposable(view.viewOnWebClicks().subscribe(irrelevant -> this.onViewOnWebClick()));
        if (view.shareClicks() != null) this.manageDisposable(view.shareClicks().subscribe(irrelevant -> this.onShareClick()));

        super.onViewAttached(view, isFirstTimeAttachment);
    }

    @Override
    protected void onBookmarkClick() {
        // TODO
    }

    @Override
    protected void onImageClick(@Nonnull @NonNull @lombok.NonNull final Image image) {
        super.onImageClick(image);
    }

    @Override
    protected void onVideoClick(@Nonnull @NonNull @lombok.NonNull final Video video) {
        super.onVideoClick(video);
    }

    private void onTextToSpeechClick() {
        // TODO
    }

    private void onViewOnWebClick() {
        // TODO
    }

    private void onShareClick() {
        // TODO
    }
}
