package com.github.ayltai.newspaper;

import javax.inject.Inject;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.github.ayltai.newspaper.config.ConfigsModule;
import com.github.ayltai.newspaper.config.DaggerConfigsComponent;
import com.github.ayltai.newspaper.media.FrescoImageLoader;
import com.github.ayltai.newspaper.view.DaggerRouterComponent;
import com.github.ayltai.newspaper.view.Router;
import com.github.ayltai.newspaper.view.RouterModule;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public final class MainActivity extends AppCompatActivity {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Inject
    Router router;

    @CallSuper
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setTheme(DaggerConfigsComponent.builder().build().userConfigs().getTheme() == Constants.THEME_DARK ? R.style.AppTheme_Dark : R.style.AppTheme_Light);

        this.setContentView(R.layout.activity_main);

        ConfigsModule.init(this);
        this.getLifecycle().addObserver(FrescoImageLoader.getInstance());
    }

    @CallSuper
    @Override
    protected void onDestroy() {
        super.onDestroy();

        this.getLifecycle().removeObserver(FrescoImageLoader.getInstance());

        if (!this.router.isDisposed()) this.router.dispose();
    }

    @CallSuper
    @Override
    public void onBackPressed() {
        if (!this.router.handleBack()) super.onBackPressed();
    }

    @Override
    protected void attachBaseContext(final Context newBase) {
        if (this.router == null) DaggerRouterComponent.builder()
            .routerModule(new RouterModule(this))
            .build()
            .inject(this);

        super.attachBaseContext(this.router.attachNewBase(ViewPumpContextWrapper.wrap(newBase)));
    }
}
