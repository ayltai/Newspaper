package com.github.ayltai.newspaper.app.view;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.app.data.model.FeaturedItem;
import com.github.ayltai.newspaper.app.widget.FeaturedView;
import com.github.ayltai.newspaper.media.BaseImageLoaderCallback;
import com.github.ayltai.newspaper.media.DaggerImageComponent;
import com.github.ayltai.newspaper.media.ImageModule;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.Views;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public class FeaturedPresenter extends ItemPresenter<FeaturedView> implements LifecycleObserver {
    private final List<Integer> requestIds = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger requestId  = new AtomicInteger(0);

    private Disposable disposable;

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    protected void onResume() {
        this.disposable = Observable.interval(Constants.FEATURED_IMAGE_ROTATION, TimeUnit.SECONDS)
            .compose(RxUtils.applyObservableBackgroundToMainSchedulers())
            .subscribe(time -> {
                if (this.getModel() instanceof FeaturedItem && this.getView() != null) {
                    ((FeaturedItem)this.getModel()).next();

                    final Integer requestId = this.requestId.incrementAndGet();
                    this.requestIds.add(requestId);

                    DaggerImageComponent.builder()
                        .imageModule(new ImageModule(this.getView().getContext()))
                        .build()
                        .imageLoader()
                        .loadImage(requestId, Uri.parse(this.getModel().getImages().get(0).getUrl()), new BaseImageLoaderCallback() {
                            @Override
                            public void onFinish() {
                                FeaturedPresenter.this.requestIds.remove(requestId);
                            }

                            @Override
                            public void onSuccess(final File image) {
                                if (FeaturedPresenter.this.getView() != null) {
                                    FeaturedPresenter.this.getView().setImages(FeaturedPresenter.this.getModel().getImages());
                                    FeaturedPresenter.this.getView().setTitle(FeaturedPresenter.this.getModel().getTitle());
                                }
                            }
                        });

                    this.bindModel(this.getModel());
                }
            });
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    protected void onPause() {
        if (this.disposable != null && !this.disposable.isDisposed()) {
            this.disposable.dispose();
            this.disposable = null;
        }
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

        this.onPause();

        this.cancelImageRequests();

        if (this.getView() != null) {
            final Activity activity = Views.getActivity(this.getView());
            if (activity instanceof AppCompatActivity) ((AppCompatActivity)activity).getLifecycle().removeObserver(this);
        }
    }

    private void cancelImageRequests() {
        if (this.getView() == null) return;

        for (final Integer requestId : this.requestIds) DaggerImageComponent.builder()
            .imageModule(new ImageModule(this.getView().getContext()))
            .build()
            .imageLoader()
            .cancel(requestId);
    }
}
