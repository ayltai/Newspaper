package com.github.ayltai.newspaper.app.widget;

import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.CallSuper;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.github.ayltai.newspaper.app.data.model.Image;
import com.github.ayltai.newspaper.app.data.model.Video;
import com.github.ayltai.newspaper.app.view.ItemPresenter;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.util.Optional;
import com.github.ayltai.newspaper.widget.BaseView;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public abstract class ItemView extends BaseView implements ItemPresenter.View {
    protected final FlowableProcessor<Optional<Point>> clicks = PublishProcessor.create();

    private final GestureDetectorCompat detector;

    protected View container;

    protected ItemView(@NonNull final Context context) {
        super(context);

        this.detector = new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(@NonNull final MotionEvent event) {
                final int[] location = new int[2];
                ItemView.this.container.getLocationOnScreen(location);

                ItemView.this.clicks.onNext(Optional.of(new Point((int)(location[0] + event.getX() + 0.5f), (int)(location[1] + event.getY() + 0.5f))));

                return super.onSingleTapConfirmed(event);
            }
        });
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

    @Override
    public void addEntity(@NonNull final String name, @NonNull final String wikiLink) {
    }

    //endregion

    //region Events

    @NonNull
    @Override
    public Flowable<Optional<Point>> clicks() {
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

    @Nullable
    @Override
    public Flowable<String> entityClicks() {
        return null;
    }

    //endregion

    @SuppressLint("NewApi")
    @CallSuper
    @Override
    public void onAttachedToWindow() {
        if (this.container != null) {
            this.container.setOnTouchListener((view, event) -> {
                this.detector.onTouchEvent(event);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) this.container
                    .getForeground()
                    .setHotspot(event.getX(), event.getY());

                this.container.setPressed(event.getActionMasked() == MotionEvent.ACTION_DOWN || event.getActionMasked() == MotionEvent.ACTION_MOVE);

                return true;
            });
        }

        super.onAttachedToWindow();
    }
}
