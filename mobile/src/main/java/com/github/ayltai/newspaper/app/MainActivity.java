package com.github.ayltai.newspaper.app;

import android.content.Context;
import android.support.v7.app.AppCompatDelegate;

import com.github.ayltai.newspaper.LifecycleActivity;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public final class MainActivity extends LifecycleActivity {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private FlowController controller;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        this.controller.onDestroy();
        this.controller = null;
    }

    @Override
    protected void attachBaseContext(final Context newBase) {
        if (this.controller == null) this.controller = new FlowController(this);

        super.attachBaseContext(this.controller.attachNewBase(ViewPumpContextWrapper.wrap(newBase)));
    }

    @Override
    public void onBackPressed() {
        if (!this.controller.onBackPressed()) super.onBackPressed();
    }
}
