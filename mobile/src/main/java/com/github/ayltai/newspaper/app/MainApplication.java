package com.github.ayltai.newspaper.app;

import java.util.Collections;

import android.os.StrictMode;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

import com.akaita.java.rxjava2debug.RxJava2Debug;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.core.CrashlyticsCore;
import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.DefaultExecutorSupplier;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig;
import com.facebook.imagepipeline.listener.RequestLoggingListener;
import com.github.ayltai.newspaper.BuildConfig;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.media.DaggerImageComponent;
import com.github.ayltai.newspaper.media.ImageModule;
import com.github.ayltai.newspaper.net.DaggerHttpComponent;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.util.ThreadPolicyFactory;
import com.github.ayltai.newspaper.util.VmPolicyFactory;
import com.github.piasy.biv.BigImageViewer;
import com.squareup.leakcanary.LeakCanary;

import io.fabric.sdk.android.Fabric;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;

public final class MainApplication extends BaseApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        this.applyDevMode();

        this.initFabric();

        RxJava2Debug.enableRxJava2AssemblyTracking(new String[] { BuildConfig.APPLICATION_ID });

        this.initFirebase();
        this.initFresco();
        this.initBigImageViewer();
        this.initCalligraphy();
    }

    private void applyDevMode() {
        if (!DevUtils.isRunningTests()) {
            StrictMode.setThreadPolicy(ThreadPolicyFactory.newThreadPolicy());
            StrictMode.setVmPolicy(VmPolicyFactory.newVmPolicy());

            if (!LeakCanary.isInAnalyzerProcess(this)) LeakCanary.install(this);
        }
    }

    private void initFabric() {
        if (!DevUtils.isLoggable() && !DevUtils.isRunningTests()) Fabric.with(this,
            new Answers(),
            new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder()
                    .disabled(DevUtils.isLoggable())
                    .build())
                .build());
    }

    private void initFirebase() {
        //noinspection CheckStyle
        try {
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(!DevUtils.isLoggable());
        } catch (final RuntimeException e) {
            if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    private void initFresco() {
        FLog.setMinimumLoggingLevel(DevUtils.isLoggable() ? FLog.INFO : FLog.ERROR);

        ImagePipelineConfig.getDefaultImageRequestConfig()
            .setProgressiveRenderingEnabled(true);

        if (!DevUtils.isRunningUnitTest()) Fresco.initialize(this, OkHttpImagePipelineConfigFactory.newBuilder(this, DaggerHttpComponent.builder()
            .build()
            .httpClient())
            .setDownsampleEnabled(true)
            .setResizeAndRotateEnabledForNetwork(true)
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
    }

    private void initBigImageViewer() {
        BigImageViewer.initialize(DaggerImageComponent.builder()
            .imageModule(new ImageModule(this))
            .build()
            .imageLoader());
    }

    private void initCalligraphy() {
        ViewPump.init(ViewPump.builder()
            .addInterceptor(new CalligraphyInterceptor(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Lato-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()))
            .build());
    }
}
