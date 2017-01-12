package com.github.ayltai.newspaper.widget;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.graphics.FaceCenterCrop;
import com.github.ayltai.newspaper.graphics.FaceDetectorFactory;
import com.github.ayltai.newspaper.util.ContextUtils;
import com.github.ayltai.newspaper.util.LogUtils;
import com.github.ayltai.newspaper.util.SuppressFBWarnings;
import com.github.piasy.biv.view.BigImageView;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public final class FaceCenteredImageView extends BigImageView {
    //region Variables

    private Subscription subscription;
    private int          screenWidth;
    private Field        mCurrentImageFile;
    private Field        mTempImages;

    //endregion

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

    @UiThread
    @Override
    public void onCacheHit(final File image) {
        this.setCurrentImageFile(image);

        if (this.subscription != null) this.subscription.unsubscribe();

        this.subscription = this.translate(image);
    }

    @WorkerThread
    @Override
    public void onCacheMiss(final File image) {
        this.setCurrentImageFile(image);
        this.getTempImages().add(image);

        if (this.subscription != null) this.subscription.unsubscribe();

        this.subscription = this.translate(image);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        ((SubsamplingScaleImageView)this.getChildAt(0)).recycle();

        final Activity activity = ContextUtils.getActivity(this.getContext());

        if (activity != null) {
            final DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

            this.screenWidth = metrics.widthPixels;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (this.subscription != null) this.subscription.unsubscribe();
        this.subscription = null;
    }

    private Subscription translate(@NonNull final File image) {
        FaceDetectorFactory.initialize(this.getContext());

        return FaceCenteredImageView.translate(image, this.screenWidth, this.getContext().getResources().getDimensionPixelSize(R.dimen.thumbnail_cozy))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(center -> {
                this.doShowImage(image);

                final SubsamplingScaleImageView imageView = (SubsamplingScaleImageView)this.getChildAt(0);
                final float                     scale     = imageView.getScale();

                imageView.resetScaleAndCenter();
                imageView.setScaleAndCenter(scale, center);
            });
    }

    @SuppressFBWarnings("MOM_MISLEADING_OVERLOAD_MODEL")
    @NonNull
    private static Observable<PointF> translate(@NonNull final File image, final int width, final int height) {
        return Observable.create(subscriber -> subscriber.onNext(new FaceCenterCrop(width, height).findCroppedCenter(image)));
    }

    //region Reflected methods

    private void setCurrentImageFile(@NonNull final File file) {
        try {
            if (this.mCurrentImageFile == null) this.mCurrentImageFile = BigImageView.class.getDeclaredField("mCurrentImageFile");
            this.mCurrentImageFile.setAccessible(true);

            this.mCurrentImageFile.set(this, file);
        } catch (final NoSuchFieldException e) {
            LogUtils.w(this.getClass().getName(), e.getMessage(), e);
        } catch (final IllegalAccessException e) {
            LogUtils.w(this.getClass().getName(), e.getMessage(), e);
        }
    }

    private List<File> getTempImages() {
        try {
            if (this.mTempImages == null) this.mTempImages = BigImageView.class.getDeclaredField("mTempImages");
            this.mTempImages.setAccessible(true);

            return (List<File>)this.mTempImages.get(this);
        } catch (final NoSuchFieldException e) {
            LogUtils.w(this.getClass().getName(), e.getMessage(), e);
        } catch (final IllegalAccessException e) {
            LogUtils.w(this.getClass().getName(), e.getMessage(), e);
        }

        return null;
    }

    private void doShowImage(@NonNull final File image) {
        ((SubsamplingScaleImageView)this.getChildAt(0)).setImage(ImageSource.uri(Uri.fromFile(image)));
    }

    //endregion
}
