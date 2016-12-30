package com.github.ayltai.newspaper.widget;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.graphics.FaceCenterCrop;
import com.github.ayltai.newspaper.util.ContextUtils;
import com.github.piasy.biv.view.BigImageView;

public final class FaceCenteredImageView extends BigImageView {
    private int screenWidth;

    //region Constructors

    public FaceCenteredImageView(final Context context) {
        super(context);
    }

    public FaceCenteredImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public FaceCenteredImageView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //endregion

    @Override
    public void onCacheHit(final File image) {
        super.onCacheHit(image);

        this.translate(image);
    }

    @Override
    public void onCacheMiss(final File image) {
        super.onCacheMiss(image);

        this.post(() -> this.translate(image));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        final Activity activity = ContextUtils.getActivity(this.getContext());

        if (activity != null) {
            final DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

            this.screenWidth = metrics.widthPixels;
        }
    }

    private void translate(@NonNull final File image) {
        final PointF center = new FaceCenterCrop(this.screenWidth, this.getContext().getResources().getDimensionPixelSize(R.dimen.thumbnail_cozy)).findCroppedCenter(image);

        final SubsamplingScaleImageView imageView = (SubsamplingScaleImageView)this.getChildAt(0);
        imageView.setScaleAndCenter(imageView.getScale(), center);
    }
}
