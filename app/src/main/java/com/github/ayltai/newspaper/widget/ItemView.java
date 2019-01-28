package com.github.ayltai.newspaper.widget;

import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;

import com.github.ayltai.newspaper.data.model.Image;
import com.github.ayltai.newspaper.data.model.Video;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.util.Optional;
import com.github.ayltai.newspaper.view.ItemPresenter;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public abstract class ItemView extends BaseView implements ItemPresenter.View {
    //region Subscriptions

    protected final FlowableProcessor<Optional<Point>> clicks            = PublishProcessor.create();
    protected final FlowableProcessor<Irrelevant>      iconClicks        = PublishProcessor.create();
    protected final FlowableProcessor<Irrelevant>      sourceClicks      = PublishProcessor.create();
    protected final FlowableProcessor<Irrelevant>      publishDateClicks = PublishProcessor.create();
    protected final FlowableProcessor<Irrelevant>      titleClicks       = PublishProcessor.create();
    protected final FlowableProcessor<Irrelevant>      descriptionClicks = PublishProcessor.create();
    protected final FlowableProcessor<Irrelevant>      linkClicks        = PublishProcessor.create();
    protected final FlowableProcessor<Integer>         imageClicks       = PublishProcessor.create();
    protected final FlowableProcessor<Integer>         videoClicks       = PublishProcessor.create();
    protected final FlowableProcessor<Irrelevant>      bookmarkClicks    = PublishProcessor.create();

    //endregion

    private final GestureDetectorCompat detector;

    protected View container;

    protected ItemView(@Nonnull @NonNull @lombok.NonNull final Context context) {
        super(context);

        this.detector = new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(@Nonnull @NonNull @lombok.NonNull final MotionEvent event) {
                final int[] location = new int[2];
                ItemView.this.container.getLocationOnScreen(location);

                ItemView.this.clicks.onNext(Optional.of(new Point((int)(location[0] + event.getX() + 0.5f), (int)(location[1] + event.getY() + 0.5f))));

                return super.onSingleTapConfirmed(event);
            }
        });
    }

    //region Properties

    @Override
    public void setIcon(@Nonnull @NonNull @lombok.NonNull final String iconUrl) {
    }

    @Override
    public void setTitle(@Nullable final CharSequence title) {
    }

    @Override
    public void setDescription(@Nullable final CharSequence description) {
    }

    @Override
    public void setSource(@Nullable final CharSequence source) {
    }

    @Override
    public void setPublishDate(@Nullable final Date date) {
    }

    @Override
    public void setLink(@Nullable final CharSequence link) {
    }

    @Override
    public void setImages(@Nonnull @NonNull @lombok.NonNull final List<Image> images) {
    }

    @Override
    public void setVideos(@Nullable final List<Video> video) {
    }

    @Override
    public void setIsBookmarked(final boolean isBookmarked) {
    }

    @Override
    public void setIsRead(final boolean isRead) {
    }

    //endregion

    //region Events

    @Nonnull
    @NonNull
    @Override
    public Flowable<Optional<Point>> clicks() {
        return this.clicks;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> iconClicks() {
        return this.iconClicks;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> sourceClicks() {
        return this.sourceClicks;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> publishDateClicks() {
        return this.publishDateClicks;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> titleClicks() {
        return this.titleClicks;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> descriptionClicks() {
        return this.descriptionClicks;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> linkClicks() {
        return this.linkClicks;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Integer> imageClicks() {
        return this.imageClicks;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Integer> videoClicks() {
        return this.videoClicks;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> bookmarkClicks() {
        return this.bookmarkClicks;
    }

    //endregion

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
