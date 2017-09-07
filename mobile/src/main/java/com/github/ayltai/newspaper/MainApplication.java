package com.github.ayltai.newspaper;

import java.util.Collections;

import android.os.StrictMode;
import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.DefaultExecutorSupplier;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig;
import com.facebook.imagepipeline.listener.RequestLoggingListener;
import com.github.ayltai.newspaper.debug.ThreadPolicyFactory;
import com.github.ayltai.newspaper.debug.VmPolicyFactory;
import com.github.ayltai.newspaper.media.FrescoImageLoader;
import com.github.ayltai.newspaper.net.DaggerHttpComponent;
import com.github.ayltai.newspaper.util.TestUtils;
import com.github.piasy.biv.BigImageViewer;
import com.squareup.leakcanary.LeakCanary;

import io.fabric.sdk.android.Fabric;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;

public final class MainApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        if (!TestUtils.isRunningTests()) {
            StrictMode.setThreadPolicy(ThreadPolicyFactory.newThreadPolicy());
            StrictMode.setVmPolicy(VmPolicyFactory.newVmPolicy());

            if (!LeakCanary.isInAnalyzerProcess(this)) LeakCanary.install(this);
        }

        Fabric.with(this, new Crashlytics.Builder()
            .core(new CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG || TestUtils.isRunningTests())
                .build())
            .build());

        FLog.setMinimumLoggingLevel(BuildConfig.DEBUG ? FLog.INFO : FLog.ERROR);

        ImagePipelineConfig.getDefaultImageRequestConfig()
            .setProgressiveRenderingEnabled(true);

        Fresco.initialize(this, OkHttpImagePipelineConfigFactory.newBuilder(this, DaggerHttpComponent.builder()
                .build()
                .httpClientBuilder()
                .build())
            .setDownsampleEnabled(true)
            .setExecutorSupplier(new DefaultExecutorSupplier(Runtime.getRuntime().availableProcessors()))
            .setProgressiveJpegConfig(new SimpleProgressiveJpegConfig())
            .setRequestListeners(Collections.singleton(new RequestLoggingListener()))
            .setMainDiskCacheConfig(DiskCacheConfig.newBuilder(this)
                .setBaseDirectoryPath(this.getCacheDir())
                .setMaxCacheSize(Constants.CACHE_SIZE_MAX)
                .setMaxCacheSizeOnLowDiskSpace(Constants.CACHE_SIZE_MAX_SMALL)
                .setMaxCacheSizeOnVeryLowDiskSpace(Constants.CACHE_SIZE_MAX_SMALLER)
                .build())
            .setSmallImageDiskCacheConfig(DiskCacheConfig.newBuilder(this)
                .setBaseDirectoryPath(this.getCacheDir())
                .setMaxCacheSize(Constants.CACHE_SIZE_MAX_SMALL)
                .setMaxCacheSizeOnLowDiskSpace(Constants.CACHE_SIZE_MAX_SMALLER)
                .setMaxCacheSizeOnVeryLowDiskSpace(Constants.CACHE_SIZE_MAX_SMALLEST)
                .build())
            .build());

        BigImageViewer.initialize(FrescoImageLoader.getInstance());

        ViewPump.init(ViewPump.builder()
            .addInterceptor(new CalligraphyInterceptor(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Lato-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()))
            .build());
    }
}
