package com.github.ayltai.newspaper.app.ads;

import java.util.EnumSet;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.github.ayltai.newspaper.ads.NativeAdManager;
import com.mopub.nativeads.MoPubStaticNativeAdRenderer;
import com.mopub.nativeads.RequestParameters;
import com.mopub.nativeads.StaticNativeAd;
import com.mopub.nativeads.ViewBinder;

public final class StaticNativeAdManager extends NativeAdManager<StaticNativeAd> {
    StaticNativeAdManager(@NonNull final Context context, @NonNull final String adUnitId, @LayoutRes final int layoutId, @IdRes final int titleTextId, @IdRes final int descriptionTextId, @IdRes final int actionTextId, @IdRes final int mainImageId, @IdRes final int iconImageId, @IdRes final int privacyImageId) {
        super(context, adUnitId, layoutId);

        final ViewBinder.Builder builder = new ViewBinder.Builder(layoutId);
        if (titleTextId > 0) builder.titleId(titleTextId);
        if (descriptionTextId > 0) builder.textId(descriptionTextId);
        if (actionTextId > 0) builder.callToActionId(actionTextId);
        if (mainImageId > 0) builder.mainImageId(mainImageId);
        if (iconImageId > 0) builder.iconImageId(iconImageId);
        if (privacyImageId > 0) builder.privacyInformationIconImageId(privacyImageId);

        this.renderer = new MoPubStaticNativeAdRenderer(builder.build());
    }

    @Override
    protected EnumSet<RequestParameters.NativeAdAsset> getDesiredAssets() {
        return EnumSet.of(RequestParameters.NativeAdAsset.TITLE, RequestParameters.NativeAdAsset.TEXT, RequestParameters.NativeAdAsset.CALL_TO_ACTION_TEXT, RequestParameters.NativeAdAsset.MAIN_IMAGE, RequestParameters.NativeAdAsset.ICON_IMAGE);
    }

    @UiThread
    public void renderAdView(@NonNull final View view) {
        final StaticNativeAd ad = this.getAd();

        if (ad == null) {
            view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
            view.setVisibility(View.GONE);
        } else {
            view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            view.setVisibility(View.VISIBLE);

            ad.prepare(view);
            this.renderer.renderAdView(view, ad);
        }
    }
}
