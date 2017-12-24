package com.github.ayltai.newspaper.app.ads;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.ads.NativeAdView;

public final class CozyNativeAdView extends NativeAdView {
    public static final int VIEW_TYPE = R.id.view_type_cozy_native_ad;

    public CozyNativeAdView(@NonNull final Context context) {
        super(context);
    }

    @Override
    protected void init() {
        super.init();

        LayoutInflater.from(this.getContext()).inflate(R.layout.view_native_ad_cozy, this, true);
    }
}
