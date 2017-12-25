package com.github.ayltai.newspaper.app.view.binding;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.github.ayltai.newspaper.ads.NativeAdPresenter;
import com.github.ayltai.newspaper.app.ads.CompactNativeAdView;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.view.binding.BindingPresenterFactory;
import com.mopub.nativeads.StaticNativeAd;

public final class CompactNativeAdBinderFactory extends BindingPresenterFactory<Item, CompactNativeAdView, NativeAdPresenter<StaticNativeAd, Item, CompactNativeAdView>> {
    private final Context context;

    public CompactNativeAdBinderFactory(@NonNull final Context context) {
        this.context = context;
    }

    @Override
    public int getPartType() {
        return CompactNativeAdView.VIEW_TYPE;
    }

    @NonNull
    @Override
    protected NativeAdPresenter<StaticNativeAd, Item, CompactNativeAdView> createPresenter() {
        final NativeAdPresenter<StaticNativeAd, Item, CompactNativeAdView> presenter = new NativeAdPresenter<>(this.context);

        if (this.context instanceof AppCompatActivity) ((AppCompatActivity)this.context).getLifecycle().addObserver(presenter);

        presenter.errors()
            .subscribe(errorCode -> {
                if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), errorCode.toString());
            });

        return presenter;
    }

    @Override
    public boolean isNeeded(@Nullable final Item model) {
        return model == null;
    }
}
