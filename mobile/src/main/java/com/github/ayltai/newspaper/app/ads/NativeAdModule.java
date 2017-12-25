package com.github.ayltai.newspaper.app.ads;

import javax.inject.Singleton;

import android.content.Context;
import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.R;

import dagger.Module;
import dagger.Provides;

@Module
public final class NativeAdModule {
    private final Context context;

    public NativeAdModule(@NonNull final Context context) {
        this.context = context;
    }

    @NonNull
    @Singleton
    @Provides
    String provideAdUnitId() {
        return this.context.getString(R.string.mopub_native_ad_unit_id);
    }

    @Singleton
    @Provides
    int provideLayoutId() {
        return R.layout.view_native_ad_cozy;
    }

    @Singleton
    @Provides
    int provideTitleTextId() {
        return R.id.title;
    }

    @Singleton
    @Provides
    int provideDescriptionTextId() {
        return R.id.description;
    }

    @Singleton
    @Provides
    int provideActionTextId() {
        return R.id.publish_date;
    }

    @Singleton
    @Provides
    int provideMainImageId() {
        return R.id.image;
    }

    @Singleton
    @Provides
    int provideIconImageId() {
        return R.id.avatar;
    }

    @Singleton
    @Provides
    int providePrivacyImageId() {
        return R.id.privacy;
    }

    @Singleton
    @Provides
    StaticNativeAdManager provideStaticNativeAdManager() {
        return new StaticNativeAdManager(this.context, this.provideAdUnitId(), this.provideLayoutId(), this.provideTitleTextId(), this.provideDescriptionTextId(), this.provideActionTextId(), this.provideMainImageId(), this.provideIconImageId(), this.providePrivacyImageId());
    }
}
