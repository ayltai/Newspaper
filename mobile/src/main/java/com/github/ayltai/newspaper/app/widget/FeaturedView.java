package com.github.ayltai.newspaper.app.widget;

import java.util.List;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.akaita.java.rxjava2debug.RxJava2Debug;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.app.data.model.Image;
import com.github.ayltai.newspaper.media.FrescoImageLoader;
import com.github.ayltai.newspaper.util.Animations;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.util.Optional;
import com.github.ayltai.newspaper.util.RxUtils;

import io.reactivex.disposables.Disposable;

public final class FeaturedView extends ItemView {
    public static final int VIEW_TYPE = R.id.view_type_featured;

    private final GestureDetectorCompat detector;

    //region Components

    private final KenBurnsView image;
    private final TextView     title;

    //endregion

    private Disposable disposable;

    public FeaturedView(@NonNull final Context context) {
        super(context);

        final View view = LayoutInflater.from(context).inflate(R.layout.view_news_featured, this, true);

        this.container = view.findViewById(R.id.container);
        this.image     = view.findViewById(R.id.featured_image);
        this.title     = view.findViewById(R.id.title);

        this.detector = new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(@NonNull final MotionEvent event) {
                final int[] location = new int[2];
                FeaturedView.this.image.getLocationOnScreen(location);

                FeaturedView.this.clicks.onNext(Optional.of(new Point((int)(location[0] + event.getX() + 0.5f), (int)(location[1] + event.getY() + 0.5f))));

                return super.onSingleTapConfirmed(event);
            }
        });
    }

    //region Properties

    @Override
    public void setTitle(@Nullable final CharSequence title) {
        if (TextUtils.isEmpty(title)) {
            this.title.setVisibility(View.GONE);
        } else {
            this.title.setVisibility(View.VISIBLE);
            this.title.setText(title);
        }
    }

    @Override
    public void setImages(@NonNull final List<Image> images) {
        if (images.isEmpty()) {
            this.image.setVisibility(View.GONE);
        } else {
            if (DevUtils.isLoggable()) Log.d(this.getClass().getSimpleName(), "Featured image = " + images.get(0).getUrl());

            this.dispose();

            this.disposable = FrescoImageLoader.loadImage(images.get(0).getUrl())
                .compose(RxUtils.applyMaybeBackgroundToMainSchedulers())
                .subscribe(
                    bitmap -> {
                        this.image.setImageBitmap(bitmap);

                        if (Animations.isEnabled()) {
                            this.image.resume();
                        } else {
                            this.image.pause();
                        }
                    },
                    error -> {
                        if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));
                    }
                );

            this.image.setVisibility(View.VISIBLE);
        }
    }

    //endregion

    @CallSuper
    @Override
    public void onAttachedToWindow() {
        this.image.resume();

        this.image.setOnTouchListener((view, event) -> {
            this.detector.onTouchEvent(event);

            return true;
        });

        super.onAttachedToWindow();
    }

    @CallSuper
    @Override
    public void onDetachedFromWindow() {
        this.image.pause();

        this.dispose();

        super.onDetachedFromWindow();
    }

    private void dispose() {
        if (this.disposable != null && !this.disposable.isDisposed()) {
            this.disposable.dispose();
            this.disposable = null;
        }
    }
}
