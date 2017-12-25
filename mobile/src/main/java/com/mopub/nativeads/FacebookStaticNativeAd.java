package com.mopub.nativeads;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.NativeAd;

public final class FacebookStaticNativeAd extends StaticNativeAd implements AdListener {
    private static final String SOCIAL_CONTEXT_FOR_AD = "socialContextForAd";

    private final Context                                     context;
    private final NativeAd                                    nativeAd;
    private final CustomEventNative.CustomEventNativeListener customEventNativeListener;

    FacebookStaticNativeAd(final Context context, @NonNull final NativeAd nativeAd, @NonNull final CustomEventNative.CustomEventNativeListener customEventNativeListener) {
        this.context                   = context.getApplicationContext();
        this.nativeAd                  = nativeAd;
        this.customEventNativeListener = customEventNativeListener;
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

    @Override
    public void onAdLoaded(final Ad ad) {
        if (this.nativeAd.equals(ad) && this.nativeAd.isAdLoaded()) {
            this.setTitle(this.nativeAd.getAdTitle());
            this.setText(this.nativeAd.getAdBody());
            this.setCallToAction(this.nativeAd.getAdCallToAction());
            this.setPrivacyInformationIconClickThroughUrl(this.nativeAd.getAdChoicesLinkUrl());
            this.setStarRating(FacebookStaticNativeAd.getDoubleRating(this.nativeAd.getAdStarRating()));
            this.addExtra(FacebookStaticNativeAd.SOCIAL_CONTEXT_FOR_AD, nativeAd.getAdSocialContext());

            if (this.nativeAd.getAdCoverImage() != null) this.setMainImageUrl(this.nativeAd.getAdCoverImage().getUrl());
            if (this.nativeAd.getAdIcon() != null) this.setIconImageUrl(this.nativeAd.getAdIcon().getUrl());
            if (this.nativeAd.getAdChoicesIcon() != null) this.setPrivacyInformationIconImageUrl(this.nativeAd.getAdChoicesIcon().getUrl());

            final List<String> imageUrls = new ArrayList<>();

            if (this.getMainImageUrl() != null) imageUrls.add(this.getMainImageUrl());
            if (this.getIconImageUrl() != null) imageUrls.add(getIconImageUrl());
            if (this.getPrivacyInformationIconImageUrl() != null) imageUrls.add(this.getPrivacyInformationIconImageUrl());

            NativeImageHelper.preCacheImages(this.context, imageUrls, new NativeImageHelper.ImageListener() {
                @Override
                public void onImagesCached() {
                    FacebookStaticNativeAd.this.customEventNativeListener.onNativeAdLoaded(FacebookStaticNativeAd.this);
                }

                @Override
                public void onImagesFailedToCache(final NativeErrorCode errorCode) {
                    FacebookStaticNativeAd.this.customEventNativeListener.onNativeAdFailed(errorCode);
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

    @Nullable
    private static Double getDoubleRating(@Nullable final NativeAd.Rating rating) {
        if (rating == null) return null;

        return FacebookStaticNativeAd.MAX_STAR_RATING * rating.getValue() / rating.getScale();
    }
}
