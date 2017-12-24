package com.github.ayltai.newspaper.app.view.binding;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.ads.NativeAdPresenter;
import com.github.ayltai.newspaper.app.ads.CozyNativeAdView;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.view.binding.BindingPresenterFactory;

public final class CozyNativeAdBinderFactory extends BindingPresenterFactory<Item, CozyNativeAdView, NativeAdPresenter<Item, CozyNativeAdView>> {
    private final Context context;
    private final String  adUnitId;

    public CozyNativeAdBinderFactory(@NonNull final Context context, @NonNull final String adUnitId) {
        this.context  = context;
        this.adUnitId = adUnitId;
    }

    @Override
    public int getPartType() {
        return CozyNativeAdView.VIEW_TYPE;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    protected NativeAdPresenter<Item, CozyNativeAdView> createPresenter() {
        final NativeAdPresenter<Item, CozyNativeAdView> presenter = new NativeAdPresenter.Builder<>(this.context, this.adUnitId)
            .titleTextId(R.id.title)
            .descriptionTextId(R.id.description)
            .mainImageId(R.id.image)
            .iconImageId(R.id.avatar)
            .build();

        if (this.context instanceof AppCompatActivity) ((AppCompatActivity)this.context).getLifecycle().addObserver(presenter);

        presenter.errors()
            .subscribe(errorCode -> {
                if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), errorCode.toString());
            });

        presenter.prefetch();

        return presenter;
    }

    @Override
    public boolean isNeeded(@Nullable final Item model) {
        return model == null;
    }
}
