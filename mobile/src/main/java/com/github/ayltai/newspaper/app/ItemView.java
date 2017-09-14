package com.github.ayltai.newspaper.app;

import java.util.Collection;
import java.util.Date;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.data.model.Image;
import com.github.ayltai.newspaper.data.model.Video;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.widget.BaseView;

import io.reactivex.Flowable;

public class ItemView extends BaseView implements ItemPresenter.View {
    public ItemView(@NonNull final Context context) {
        super(context);
    }

    @Override
    public void setAvatar(@Nullable final String avatarUri) {
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
    public void setImages(@NonNull final Collection<Image> images) {
    }

    @Override
    public void setVideo(@Nullable final Video video) {
    }

    @Nullable
    @Override
    public Flowable<Irrelevant> clicks() {
        return null;
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
    public Flowable<Boolean> bookmarkClicks() {
        return null;
    }

    @Nullable
    @Override
    public Flowable<Image> imageClicks() {
        return null;
    }

    @Nullable
    @Override
    public Flowable<Irrelevant> videoClick() {
        return null;
    }
}
