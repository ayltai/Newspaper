package com.github.ayltai.newspaper.app.view;

import java.io.File;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.akaita.java.rxjava2debug.RxJava2Debug;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.app.data.model.FeaturedItem;
import com.github.ayltai.newspaper.app.widget.FeaturedView;
import com.github.ayltai.newspaper.media.DaggerImageComponent;
import com.github.ayltai.newspaper.media.ImageModule;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.Views;
import com.github.piasy.biv.loader.ImageLoader;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public class FeaturedPresenter extends ItemPresenter<FeaturedView> implements LifecycleObserver {
    private Disposable disposable;

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    protected void onResume() {
        this.disposable = Observable.interval(Constants.FEATURED_IMAGE_ROTATION, TimeUnit.SECONDS)
            .compose(RxUtils.applyObservableBackgroundToMainSchedulers())
            .subscribe(time -> {
                if (this.getModel() instanceof FeaturedItem && this.getView() != null) {
                    ((FeaturedItem)this.getModel()).next();

                    DaggerImageComponent.builder()
                        .imageModule(new ImageModule(this.getView().getContext()))
                        .build()
                        .imageLoader()
                        .loadImage(Uri.parse(this.getModel().getImages().get(0).getUrl()), new ImageLoader.Callback() {
                            @Override
                            public void onCacheHit(final File image) {
                            }

                            @Override
                            public void onCacheMiss(final File image) {
                            }

                            @Override
                            public void onStart() {
                            }

                            @Override
                            public void onProgress(final int progress) {
                            }

                            @Override
                            public void onFinish() {
                            }

                            @Override
                            public void onSuccess(final File image) {
                                if (FeaturedPresenter.this.getView() != null) {
                                    FeaturedPresenter.this.getView().setImages(FeaturedPresenter.this.getModel().getImages());
                                    FeaturedPresenter.this.getView().setTitle(FeaturedPresenter.this.getModel().getTitle());
                                }
                            }

                            @Override
                            public void onFail(final Exception error) {
                                if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));
                            }
                        });

                    this.bindModel(this.getModel());
                }
            });
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    protected void onPause() {
        if (this.disposable != null && !this.disposable.isDisposed()) this.disposable.dispose();
    }

    @Override
    public void onViewAttached(@NonNull final FeaturedView view, final boolean isFirstTimeAttachment) {
        super.onViewAttached(view, isFirstTimeAttachment);

        final Activity activity = Views.getActivity(view);
        if (activity instanceof AppCompatActivity) ((AppCompatActivity)activity).getLifecycle().addObserver(this);
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();

        if (this.getView() != null) {
            final Activity activity = Views.getActivity(this.getView());
            if (activity instanceof AppCompatActivity) ((AppCompatActivity)activity).getLifecycle().removeObserver(this);
        }
    }
}
