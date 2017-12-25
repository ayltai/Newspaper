package com.github.ayltai.newspaper.app.ads;

import javax.inject.Singleton;

import android.support.annotation.NonNull;

import dagger.Component;

@Singleton
@Component(modules = { NativeAdModule.class })
public interface NativeAdComponent {
    @NonNull
    StaticNativeAdManager staticNativeAdManager();
}
