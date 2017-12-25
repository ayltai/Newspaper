package com.mopub.nativeads;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.ads.NativeAd;

public final class FacebookNative extends CustomEventNative {
    private static final String PLACEMENT_ID_KEY  = "placement_id";
    private static final String VIDEO_ENABLED_KEY = "video_enabled";

    private static boolean isVideoEnabled           = false;
    private static Boolean isVideoRendererAvailable = null;

    public static void setVideoEnabled(final boolean isVideoEnabled) {
        FacebookNative.isVideoEnabled = isVideoEnabled;
    }

    public static void setVideoRendererAvailable(final boolean videoRendererAvailable) {
        FacebookNative.isVideoRendererAvailable = videoRendererAvailable;
    }

    @Nullable
    public static Boolean isVideoRendererAvailable() {
        return FacebookNative.isVideoRendererAvailable;
    }

    @Override
    protected void loadNativeAd(@NonNull final Context context, @NonNull final CustomEventNativeListener customEventNativeListener, @NonNull final Map<String, Object> localExtras, @NonNull final Map<String, String> serverExtras) {
        if (TextUtils.isEmpty(serverExtras.get(FacebookNative.PLACEMENT_ID_KEY))) {
            customEventNativeListener.onNativeAdFailed(NativeErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
        } else {
            final String  placementId        = serverExtras.get(FacebookNative.PLACEMENT_ID_KEY);
            final String  videoEnabledString = serverExtras.get(FacebookNative.VIDEO_ENABLED_KEY);
            final boolean videoEnabled       = videoEnabledString != null && Boolean.parseBoolean(videoEnabledString);

            if (FacebookNative.isVideoRendererAvailable == null) {
                try {
                    Class.forName("com.mopub.nativeads.FacebookAdRenderer");
                    FacebookNative.isVideoRendererAvailable = true;
                } catch (final ClassNotFoundException e) {
                    FacebookNative.isVideoRendererAvailable = false;
                }
            }

            if (FacebookNative.isVideoEnabled && FacebookNative.isVideoRendererAvailable && videoEnabled) {
                new FacebookVideoEnabledNativeAd(context, new NativeAd(context, placementId), customEventNativeListener).loadAd();
            } else {
                new FacebookStaticNativeAd(context, new NativeAd(context, placementId), customEventNativeListener).loadAd();
            }
        }
    }

    static void registerChildViewsForInteraction(@NonNull final View view, @Nullable final NativeAd nativeAd) {
        if (nativeAd != null) {
            final List<View> clickableViews = new ArrayList<>();

            FacebookNative.assembleChildViews(view, clickableViews);

            if (clickableViews.size() == 1) {
                nativeAd.registerViewForInteraction(view);
            } else {
                nativeAd.registerViewForInteraction(view, clickableViews);
            }
        }
    }

    private static void assembleChildViews(@NonNull final View view, @NonNull final List<View> clickableViews) {
        if (view instanceof ViewGroup) {
            final ViewGroup parent = (ViewGroup)view;
            for (int i = 0; i < parent.getChildCount(); i++) assembleChildViews(parent.getChildAt(i), clickableViews);
        } else {
            clickableViews.add(view);
        }
    }
}
