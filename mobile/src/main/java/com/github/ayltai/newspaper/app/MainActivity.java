package com.github.ayltai.newspaper.app;

import javax.inject.Inject;

import android.arch.lifecycle.LifecycleObserver;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FrameMetricsAggregator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MenuItem;

import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.analytics.AnalyticsModule;
import com.github.ayltai.newspaper.analytics.AppOpenEvent;
import com.github.ayltai.newspaper.analytics.Attribute;
import com.github.ayltai.newspaper.analytics.DaggerAnalyticsComponent;
import com.github.ayltai.newspaper.app.config.ConfigModule;
import com.github.ayltai.newspaper.app.config.DaggerConfigComponent;
import com.github.ayltai.newspaper.app.config.RemoteConfig;
import com.github.ayltai.newspaper.app.config.UserConfig;
import com.github.ayltai.newspaper.data.DaggerDataComponent;
import com.github.ayltai.newspaper.data.DataManager;
import com.github.ayltai.newspaper.data.DataModule;
import com.github.ayltai.newspaper.media.DaggerImageComponent;
import com.github.ayltai.newspaper.media.ImageModule;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.TestUtils;
import com.github.piasy.biv.loader.ImageLoader;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import io.reactivex.Single;
import io.realm.Realm;

public final class MainActivity extends AppCompatActivity {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Inject RemoteConfig remoteConfig;
    @Inject UserConfig   userConfig;

    private FlowController controller;
    private Realm          realm;

    //region Performance monitoring

    private Trace                  trace;
    private FrameMetricsAggregator aggregator;

    //endregion

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerConfigComponent.builder()
            .configModule(new ConfigModule(this))
            .build()
            .inject(this);

        this.setTheme(this.userConfig.getTheme() == Constants.THEME_LIGHT ? R.style.AppTheme_Light : R.style.AppTheme_Dark);

        Single.<Realm>create(emitter -> emitter.onSuccess(DaggerDataComponent.builder()
            .dataModule(new DataModule(this))
            .build()
            .realm()))
            .compose(RxUtils.applySingleSchedulers(DataManager.SCHEDULER))
            .subscribe(realm -> this.realm = realm);

        this.initImageLoader();

        DaggerAnalyticsComponent.builder()
            .analyticsModule(new AnalyticsModule(this))
            .build()
            .eventLogger()
            .logEvent(new AppOpenEvent()
                .addAttribute(new Attribute("Settings - Cozy Layout", String.valueOf(this.userConfig.getViewStyle() == Constants.VIEW_STYLE_COZY)))
                .addAttribute(new Attribute("Settings - Dark Theme", String.valueOf(this.userConfig.getTheme() == Constants.THEME_DARK)))
                .addAttribute(new Attribute("Settings - Auto Play", String.valueOf(this.userConfig.isAutoPlayEnabled())))
                .addAttribute(new Attribute("Settings - Panorama", String.valueOf(this.userConfig.isPanoramaEnabled()))));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (TestUtils.isLoggable()) {
            this.trace      = FirebasePerformance.getInstance().newTrace(this.getClass().getSimpleName());
            this.aggregator = new FrameMetricsAggregator(FrameMetricsAggregator.TOTAL_DURATION);
            this.aggregator.add(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (TestUtils.isLoggable()) {
            try {
                final SparseIntArray totalDurations = this.aggregator.getMetrics()[FrameMetricsAggregator.TOTAL_INDEX];

                for (int i = 0; i < totalDurations.size(); i++) {
                    this.trace.incrementCounter("frames");
                    if (totalDurations.get(i) > Constants.DURATION_SLOW_FRAME) this.trace.incrementCounter("slow_frames");
                    if (totalDurations.get(i) > Constants.DURATION_FROZEN_FRAME) this.trace.incrementCounter("frozen_frames");
                }
            } catch (final NullPointerException e) {
                if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
            }

            this.trace.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        this.controller.onDestroy();
        this.controller = null;

        if (this.isFinishing()) {
            if (this.realm != null) {
                Single.<Irrelevant>create(
                    emitter -> {
                        this.realm.close();

                        emitter.onSuccess(Irrelevant.INSTANCE);
                    })
                    .compose(RxUtils.applySingleSchedulers(DataManager.SCHEDULER))
                    .subscribe();
            }
        }
    }

    @Override
    protected void attachBaseContext(final Context newBase) {
        if (this.controller == null) this.controller = new FlowController(this);

        super.attachBaseContext(this.controller.attachNewBase(ViewPumpContextWrapper.wrap(newBase)));
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.onBackPressed();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!this.controller.onBackPressed()) super.onBackPressed();
    }

    private void initImageLoader() {
        final ImageLoader imageLoader = DaggerImageComponent.builder()
            .imageModule(new ImageModule(this))
            .build()
            .imageLoader();

        if (imageLoader instanceof LifecycleObserver) this.getLifecycle().addObserver((LifecycleObserver)imageLoader);
    }
}
