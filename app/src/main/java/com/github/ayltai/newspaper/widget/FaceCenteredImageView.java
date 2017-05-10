package com.github.ayltai.newspaper.widget;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.util.AttributeSet;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.graphics.FaceCenterCrop;
import com.github.ayltai.newspaper.graphics.FaceDetectorFactory;
import com.github.ayltai.newspaper.graphics.ScaleCenter;
import com.github.ayltai.newspaper.util.LogUtils;
import com.github.ayltai.newspaper.util.SuppressFBWarnings;
import com.github.piasy.biv.view.BigImageView;

import rx.Emitter;
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

    public void setScreenWidth(final int screenWidth) {
        this.screenWidth = screenWidth;
    }

    @UiThread
    @Override
    public void onCacheHit(final File image) {
        this.setCurrentImageFile(image);

        if (this.subscription != null) this.subscription.unsubscribe();

        this.subscription = this.translate(image);
    }

    @SuppressWarnings("WrongThread")
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
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (this.subscription != null) {
            this.subscription.unsubscribe();
            this.subscription = null;
        }
    }

    private Subscription translate(@NonNull final File image) {
        FaceDetectorFactory.initialize(this.getContext());

        return FaceCenteredImageView.translate(image, this.screenWidth, this.getContext().getResources().getDimensionPixelSize(R.dimen.thumbnail_cozy))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(scaleCenter -> {
                this.doShowImage(image);

                final SubsamplingScaleImageView imageView = (SubsamplingScaleImageView)this.getChildAt(0);

                imageView.setScaleAndCenter(scaleCenter.scale, scaleCenter.center);
            });
    }

    @SuppressFBWarnings("MOM_MISLEADING_OVERLOAD_MODEL")
    @NonNull
    private static Observable<ScaleCenter> translate(@NonNull final File image, final int width, final int height) {
        return Observable.create(emitter -> emitter.onNext(new FaceCenterCrop(width, height).findCroppedCenter(image)), Emitter.BackpressureMode.BUFFER);
    }

    //region Reflected methods

    @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
    private void setCurrentImageFile(@NonNull final File file) {
        try {
            if (this.mCurrentImageFile == null) this.mCurrentImageFile = BigImageView.class.getDeclaredField("mCurrentImageFile");
            this.mCurrentImageFile.setAccessible(true);

            this.mCurrentImageFile.set(this, file);
        } catch (final NoSuchFieldException e) {
            LogUtils.getInstance().w(this.getClass().getSimpleName(), e.getMessage(), e);
        } catch (final IllegalAccessException e) {
            LogUtils.getInstance().w(this.getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
    private List<File> getTempImages() {
        try {
            if (this.mTempImages == null) this.mTempImages = BigImageView.class.getDeclaredField("mTempImages");
            this.mTempImages.setAccessible(true);

            return (List<File>)this.mTempImages.get(this);
        } catch (final NoSuchFieldException e) {
            LogUtils.getInstance().w(this.getClass().getSimpleName(), e.getMessage(), e);
        } catch (final IllegalAccessException e) {
            LogUtils.getInstance().w(this.getClass().getSimpleName(), e.getMessage(), e);
        }

        return null;
    }

    private void doShowImage(@NonNull final File image) {
        ((SubsamplingScaleImageView)this.getChildAt(0)).setImage(ImageSource.uri(Uri.fromFile(image)));
    }

    //endregion
}
