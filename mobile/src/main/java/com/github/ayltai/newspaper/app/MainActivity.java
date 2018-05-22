package com.github.ayltai.newspaper.app;

import javax.inject.Inject;

import android.arch.lifecycle.LifecycleObserver;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.analytics.AppOpenEvent;
import com.github.ayltai.newspaper.analytics.Attribute;
import com.github.ayltai.newspaper.app.config.UserConfig;
import com.github.ayltai.newspaper.data.DataManager;
import com.github.ayltai.newspaper.media.FaceCenterFinder;
import com.github.ayltai.newspaper.util.ContextUtils;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.view.RxFlow;
import com.github.piasy.biv.loader.ImageLoader;

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextUtils.getColor(this, R.attr.primaryColorDark));
        }

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
                .addAttribute(new Attribute("Settings - Auto Play", String.valueOf(this.userConfig.isAutoPlayEnabled()))));
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
