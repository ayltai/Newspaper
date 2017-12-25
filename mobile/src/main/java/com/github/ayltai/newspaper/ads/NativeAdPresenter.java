package com.github.ayltai.newspaper.ads;

import java.util.Map;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.app.ComponentFactory;
import com.github.ayltai.newspaper.view.binding.BindingPresenter;
import com.github.ayltai.newspaper.widget.BaseView;
import com.mopub.nativeads.BaseNativeAd;
import com.mopub.nativeads.NativeErrorCode;

import io.reactivex.Flowable;

public class NativeAdPresenter<T extends BaseNativeAd, M, V extends BaseView> extends BindingPresenter<M, V> implements LifecycleObserver {
    private final NativeAdManager<T> manager;

    @SuppressWarnings("unchecked")
    public NativeAdPresenter(@NonNull final Context context) {
        this.manager = (NativeAdManager<T>)ComponentFactory.getInstance()
            .getNativeAdComponent(context)
            .staticNativeAdManager();

        this.manager.init();
        this.manager.prefetch();
    }

    public int getPrefetchSize() {
        return this.manager.getPrefetchSize();
    }

    public void setPrefetchSize(final int prefetchSize) {
        this.manager.setPrefetchSize(prefetchSize);
    }

    @NonNull
    public Map<String, String> getKeywords() {
        return this.manager.getKeywords();
    }

    public void addKeywords(@NonNull final String key, @Nullable final String value) {
        this.manager.addKeyword(key, value);
    }

    @Override
    public boolean isDisposed() {
        return this.manager.isDisposed();
    }

    @NonNull
    public Flowable<NativeErrorCode> errors() {
        return this.manager.errors();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    @Override
    public void dispose() {
        this.manager.dispose();
    }

    @Override
    public void onViewAttached(@NonNull final V view, final boolean isFirstTimeAttachment) {
        super.onViewAttached(view, isFirstTimeAttachment);

        this.manager.renderAdView(view);
    }
}
