package com.github.ayltai.newspaper.app.widget;

import java.util.Date;
import java.util.List;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.github.ayltai.newspaper.app.view.ItemPresenter;
import com.github.ayltai.newspaper.app.data.model.Image;
import com.github.ayltai.newspaper.app.data.model.Video;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.widget.ObservableView;
import com.jakewharton.rxbinding2.view.RxView;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public abstract class ItemView extends ObservableView implements ItemPresenter.View {
    protected final FlowableProcessor<Irrelevant> clicks = PublishProcessor.create();

    protected View container;

    protected ItemView(@NonNull final Context context) {
        super(context);
    }

    //region Properties

    @Override
    public void setAvatar(@DrawableRes final int avatar) {
    }

    @Override
    public void setSource(@Nullable final CharSequence source) {
    }

    @Override
    public void setPublishDate(@Nullable final Date date) {
    }

    @Override
    public void setTitle(@Nullable final CharSequence title) {
    }

    @Override
    public void setDescription(@Nullable final CharSequence description) {
    }

    @Override
    public void setLink(@Nullable final CharSequence link) {
    }

    @Override
    public void setIsBookmarked(final boolean isBookmarked) {
    }

    @Override
    public void setImages(@NonNull final List<Image> images) {
    }

    @Override
    public void setVideo(@Nullable final Video video) {
    }

    @Override
    public void setIsRead(final boolean isRead) {
    }

    //endregion

    //region Events

    @NonNull
    @Override
    public Flowable<Irrelevant> clicks() {
        return this.clicks;
    }

    @Nullable
    @Override
    public Flowable<Irrelevant> avatarClicks() {
        return null;
    }

    @Nullable
    @Override
    public Flowable<Irrelevant> sourceClicks() {
        return null;
    }

    @Nullable
    @Override
    public Flowable<Irrelevant> publishDateClicks() {
        return null;
    }

    @Nullable
    @Override
    public Flowable<Irrelevant> titleClicks() {
        return null;
    }

    @Nullable
    @Override
    public Flowable<Irrelevant> descriptionClicks() {
        return null;
    }

    @Nullable
    @Override
    public Flowable<Irrelevant> linkClicks() {
        return null;
    }

    @Nullable
    @Override
    public Flowable<Irrelevant> bookmarkClicks() {
        return null;
    }

    @Nullable
    @Override
    public Flowable<Image> imageClicks() {
        return null;
    }

    @Nullable
    @Override
    public Flowable<Irrelevant> videoClicks() {
        return null;
    }

    //endregion

    @CallSuper
    @Override
    public void onAttachedToWindow() {
        if (this.container != null) this.manageDisposable(RxView.clicks(this.container).subscribe(irrelevant -> this.clicks.onNext(Irrelevant.INSTANCE)));

        super.onAttachedToWindow();
    }
}
