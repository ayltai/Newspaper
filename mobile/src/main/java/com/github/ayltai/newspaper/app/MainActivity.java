package com.github.ayltai.newspaper.app;

import javax.inject.Inject;

import android.arch.lifecycle.LifecycleObserver;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.app.FrameMetricsAggregator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import com.akaita.java.rxjava2debug.RxJava2Debug;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.analytics.AppOpenEvent;
import com.github.ayltai.newspaper.analytics.Attribute;
import com.github.ayltai.newspaper.app.config.UserConfig;
import com.github.ayltai.newspaper.data.DataManager;
import com.github.ayltai.newspaper.media.FaceCenterFinder;
import com.github.ayltai.newspaper.util.ContextUtils;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.view.RxFlow;
import com.github.piasy.biv.loader.ImageLoader;
import com.instabug.library.Instabug;
import com.instabug.library.InstabugColorTheme;
import com.instabug.library.InstabugCustomTextPlaceHolder;
import com.instabug.library.InstabugTrackingDelegate;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import io.reactivex.Single;
import io.realm.Realm;

public final class MainActivity extends AppCompatActivity {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Inject UserConfig userConfig;

    private RxFlow flow;
    private Realm  realm;

    //region Performance monitoring

    private Trace                  trace;
    private FrameMetricsAggregator aggregator;

    //endregion

    @CallSuper
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        this.setContentView(R.layout.activity_main);

        ComponentFactory.init();
        this.getLifecycle().addObserver(ComponentFactory.getInstance());

        super.onCreate(savedInstanceState);

        ComponentFactory.getInstance()
            .getConfigComponent(this)
            .inject(this);

        this.setTheme(this.userConfig.getTheme() == Constants.THEME_LIGHT ? R.style.AppTheme_Light : R.style.AppTheme_Dark);

        if (!DevUtils.isRunningUnitTest()) this.initInstabug();

        Single.<Realm>create(emitter -> emitter.onSuccess(ComponentFactory.getInstance()
            .getDataComponent(this)
            .realm()))
            .compose(RxUtils.applySingleSchedulers(DataManager.SCHEDULER))
            .subscribe(realm -> this.realm = realm);

        this.initImageModule();

        ComponentFactory.getInstance()
            .getAnalyticsComponent(this)
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

        if (DevUtils.isLoggable()) {
            this.trace      = FirebasePerformance.getInstance().newTrace(this.getClass().getSimpleName());
            this.aggregator = new FrameMetricsAggregator(FrameMetricsAggregator.TOTAL_DURATION);
            this.aggregator.add(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (DevUtils.isLoggable()) {
            try {
                final SparseIntArray totalDurations = this.aggregator.getMetrics()[FrameMetricsAggregator.TOTAL_INDEX];

                for (int i = 0; i < totalDurations.size(); i++) {
                    this.trace.incrementCounter("frames");
                    if (totalDurations.get(i) > Constants.DURATION_SLOW_FRAME) this.trace.incrementCounter("slow_frames");
                    if (totalDurations.get(i) > Constants.DURATION_FROZEN_FRAME) this.trace.incrementCounter("frozen_frames");
                }
            } catch (final NullPointerException e) {
                if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), e.getMessage(), RxJava2Debug.getEnhancedStackTrace(e));
            }

            this.trace.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        this.flow.onDestroy();
        this.flow = null;

        if (this.isFinishing()) {
            if (this.realm != null) Single.<Irrelevant>create(
                emitter -> {
                    this.realm.close();

                    if (!emitter.isDisposed()) emitter.onSuccess(Irrelevant.INSTANCE);
                })
                .compose(RxUtils.applySingleSchedulers(DataManager.SCHEDULER))
                .subscribe();
        }

        this.disposeImageModule();

        this.getLifecycle().removeObserver(ComponentFactory.getInstance());
    }

    @Override
    protected void attachBaseContext(final Context newBase) {
        if (this.flow == null) this.flow = new MainFlow(this);

        super.attachBaseContext(this.flow.attachNewBase(ViewPumpContextWrapper.wrap(newBase)));
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
        if (!this.flow.onBackPressed()) super.onBackPressed();
    }

    @Override
    public boolean dispatchTouchEvent(final MotionEvent event) {
        InstabugTrackingDelegate.notifyActivityGotTouchEvent(event, this);

        return super.dispatchTouchEvent(event);
    }

    private void initInstabug() {
        Instabug.setTheme(this.userConfig.getTheme() == Constants.THEME_LIGHT ? InstabugColorTheme.InstabugColorThemeLight : InstabugColorTheme.InstabugColorThemeDark);
        Instabug.setPrimaryColor(ContextUtils.getColor(this, R.attr.primaryColor));
        Instabug.setAttachmentTypesEnabled(false, true, true, false, false);
        Instabug.setChatNotificationEnabled(false);
        Instabug.setCommentFieldRequired(true);
        Instabug.setEnableInAppNotificationSound(false);
        Instabug.setEnableSystemNotificationSound(false);
        Instabug.setIntroMessageEnabled(false);
        Instabug.setPromptOptionsEnabled(false, true, true);
        Instabug.setShouldAudioRecordingOptionAppear(false);
        Instabug.setShouldPlayConversationSounds(false);
        Instabug.setWillSkipScreenshotAnnotation(true);

        final InstabugCustomTextPlaceHolder placeHolder = new InstabugCustomTextPlaceHolder();
        placeHolder.set(InstabugCustomTextPlaceHolder.Key.INVOCATION_HEADER, this.getString(R.string.instabug_report_header));
        placeHolder.set(InstabugCustomTextPlaceHolder.Key.BUG_REPORT_HEADER, this.getString(R.string.instabug_bug_report_header));
        placeHolder.set(InstabugCustomTextPlaceHolder.Key.REPORT_BUG, this.getString(R.string.instabug_report_bug));
        placeHolder.set(InstabugCustomTextPlaceHolder.Key.COMMENT_FIELD_HINT_FOR_BUG_REPORT, this.getString(R.string.instabug_bug_report_hint));
        placeHolder.set(InstabugCustomTextPlaceHolder.Key.FEEDBACK_REPORT_HEADER, this.getString(R.string.instabug_feedback_report_header));
        placeHolder.set(InstabugCustomTextPlaceHolder.Key.REPORT_FEEDBACK, this.getString(R.string.instabug_report_feedback));
        placeHolder.set(InstabugCustomTextPlaceHolder.Key.COMMENT_FIELD_HINT_FOR_FEEDBACK, this.getString(R.string.instabug_feedback_report_hint));
        placeHolder.set(InstabugCustomTextPlaceHolder.Key.EMAIL_FIELD_HINT, this.getString(R.string.instabug_email_hint));
        placeHolder.set(InstabugCustomTextPlaceHolder.Key.ADD_EXTRA_SCREENSHOT, this.getString(R.string.instabug_screenshot));
        placeHolder.set(InstabugCustomTextPlaceHolder.Key.ADD_IMAGE_FROM_GALLERY, this.getString(R.string.instabug_gallery));
        Instabug.setCustomTextPlaceHolders(placeHolder);
    }

    private void initImageModule() {
        final ImageLoader imageLoader = ComponentFactory.getInstance().getImageComponent(this).imageLoader();
        if (imageLoader instanceof LifecycleObserver) this.getLifecycle().addObserver((LifecycleObserver)imageLoader);

        final FaceCenterFinder faceCenterFinder = ComponentFactory.getInstance().getImageComponent(this).faceCenterFinder();
        this.getLifecycle().addObserver(faceCenterFinder);
    }

    private void disposeImageModule() {
        final ImageLoader imageLoader = ComponentFactory.getInstance().getImageComponent(this).imageLoader();
        if (imageLoader instanceof LifecycleObserver) this.getLifecycle().removeObserver((LifecycleObserver)imageLoader);

        final FaceCenterFinder faceCenterFinder = ComponentFactory.getInstance().getImageComponent(this).faceCenterFinder();
        this.getLifecycle().removeObserver(faceCenterFinder);
    }
}
