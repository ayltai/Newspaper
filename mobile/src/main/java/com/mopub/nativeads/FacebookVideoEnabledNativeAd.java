package com.mopub.nativeads;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;

import gnu.trove.map.hash.THashMap;

public final class FacebookVideoEnabledNativeAd extends BaseNativeAd implements AdListener {
    private final Context                                     context;
    private final NativeAd                                    nativeAd;
    private final CustomEventNative.CustomEventNativeListener customEventNativeListener;
    private final Map<String, Object>                         extras = new THashMap<>();

    FacebookVideoEnabledNativeAd(final Context context, @NonNull final NativeAd nativeAd, @NonNull final CustomEventNative.CustomEventNativeListener customEventNativeListener) {
        this.context                   = context.getApplicationContext();
        this.nativeAd                  = nativeAd;
        this.customEventNativeListener = customEventNativeListener;
    }

    String getTitle() {
        return this.nativeAd.getAdTitle();
    }

    String getText() {
        return this.nativeAd.getAdBody();
    }

    @Nullable
    String getMainImageUrl() {
        return this.nativeAd.getAdCoverImage() == null ? null : this.nativeAd.getAdCoverImage().getUrl();
    }

    @Nullable
    String getIconImageUrl() {
        return this.nativeAd.getAdIcon() == null ? null : this.nativeAd.getAdIcon().getUrl();
    }

    String getCallToAction() {
        return this.nativeAd.getAdCallToAction();
    }

    String getPrivacyInformationIconClickThroughUrl() {
        return this.nativeAd.getAdChoicesLinkUrl();
    }

    @Nullable
    String getPrivacyInformationIconImageUrl() {
        return this.nativeAd.getAdChoicesIcon() == null ? null : this.nativeAd.getAdChoicesIcon().getUrl();
    }

    @NonNull
    Map<String, Object> getExtras() {
        return new THashMap<>(this.extras);
    }

    @Override
    public void prepare(@NonNull final View view) {
        FacebookNative.registerChildViewsForInteraction(view, this.nativeAd);
    }

    @Override
    public void clear(@NonNull final View view) {
        this.nativeAd.unregisterView();
    }

    @Override
    public void destroy() {
        this.nativeAd.destroy();
    }

    void loadAd() {
        this.nativeAd.setAdListener(this);
        this.nativeAd.loadAd();
    }

    void updateMediaView(@Nullable final MediaView mediaView) {
        if (mediaView != null) mediaView.setNativeAd(this.nativeAd);
    }

    @Override
    public void onAdLoaded(final Ad ad) {
        if (this.nativeAd.equals(ad) || this.nativeAd.isAdLoaded()) {
            final List<String> imageUrls = new ArrayList<>();

            if (this.nativeAd.getAdCoverImage() != null) imageUrls.add(this.nativeAd.getAdCoverImage().getUrl());
            if (this.nativeAd.getAdIcon() != null) imageUrls.add(this.nativeAd.getAdIcon().getUrl());
            if (this.nativeAd.getAdChoicesIcon() != null) imageUrls.add(this.nativeAd.getAdChoicesIcon().getUrl());

            NativeImageHelper.preCacheImages(this.context, imageUrls, new NativeImageHelper.ImageListener() {
                @Override
                public void onImagesCached() {
                    FacebookVideoEnabledNativeAd.this.customEventNativeListener.onNativeAdLoaded(FacebookVideoEnabledNativeAd.this);
                }

                @Override
                public void onImagesFailedToCache(final NativeErrorCode errorCode) {
                    FacebookVideoEnabledNativeAd.this.customEventNativeListener.onNativeAdFailed(errorCode);
                }
            });
        } else {
            this.customEventNativeListener.onNativeAdFailed(NativeErrorCode.NETWORK_INVALID_STATE);
        }
    }

    @Override
    public void onError(final Ad ad, @Nullable final AdError adError) {
        if (adError == null) {
            this.customEventNativeListener.onNativeAdFailed(NativeErrorCode.UNSPECIFIED);
        } else if (adError.getErrorCode() == AdError.NO_FILL_ERROR_CODE) {
            this.customEventNativeListener.onNativeAdFailed(NativeErrorCode.NETWORK_NO_FILL);
        } else if (adError.getErrorCode() == AdError.INTERNAL_ERROR_CODE) {
            this.customEventNativeListener.onNativeAdFailed(NativeErrorCode.NETWORK_INVALID_STATE);
        } else if (adError.getErrorCode() == AdError.NETWORK_ERROR_CODE) {
            this.customEventNativeListener.onNativeAdFailed(NativeErrorCode.CONNECTION_ERROR);
        } else {
            this.customEventNativeListener.onNativeAdFailed(NativeErrorCode.UNSPECIFIED);
        }
    }

    @Override
    public void onAdClicked(final Ad ad) {
        this.notifyAdClicked();
    }

    @Override
    public void onLoggingImpression(final Ad ad) {
        this.notifyAdImpressed();
    }
}
