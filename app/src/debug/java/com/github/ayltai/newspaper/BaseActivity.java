package com.github.ayltai.newspaper;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.ayltai.newspaper.setting.Settings;
import com.github.ayltai.newspaper.util.TestUtils;
import com.github.ayltai.newspaper.util.ViewUtils;

import io.palaima.debugdrawer.DebugDrawer;
import io.palaima.debugdrawer.actions.ActionsModule;
import io.palaima.debugdrawer.actions.SwitchAction;
import io.palaima.debugdrawer.commons.BuildModule;
import io.palaima.debugdrawer.commons.DeviceModule;
import io.palaima.debugdrawer.commons.NetworkModule;
import io.palaima.debugdrawer.commons.SettingsModule;
import io.palaima.debugdrawer.fps.FpsModule;
import jp.wasabeef.takt.Seat;
import jp.wasabeef.takt.Takt;

public abstract class BaseActivity extends AppCompatActivity {
    //region Variables

    private static int     referenceCount;
    private static boolean pendingCreation;

    private Takt.Program program;
    private DebugDrawer  debugDrawer;

    //endregion

    //region Lifecycle

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!TestUtils.isRunningInstrumentedTest()) {
            if (BaseActivity.referenceCount > 0) {
                this.stopTakt();

                BaseActivity.pendingCreation = true;
            } else {
                this.program = this.startTakt();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (!TestUtils.isRunningInstrumentedTest()) {
            if (BaseActivity.pendingCreation) {
                BaseActivity.pendingCreation = false;

                this.program = this.startTakt();
            } else {
                this.stopTakt();
            }
        }

        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (this.debugDrawer != null) this.debugDrawer.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (this.debugDrawer != null) this.debugDrawer.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (this.debugDrawer != null) this.debugDrawer.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (this.debugDrawer != null) this.debugDrawer.onPause();
    }

    //endregion

    @Override
    public void setContentView(final View view) {
        final View parent = ViewUtils.getRootView(view);

        if (parent.getParent() == null) super.setContentView(parent);

        if (this.debugDrawer == null && this.program != null) this.setUpDebugDrawer(this.program);
    }

    @NonNull
    private Takt.Program startTakt() {
        BaseActivity.referenceCount++;

        return Takt.stock(this.getApplication()).seat(Seat.TOP_RIGHT).color(Color.WHITE);
    }

    private void stopTakt() {
        try {
            Takt.finish();

            this.program = null;
        } catch (final IllegalArgumentException | NullPointerException e) {
            // Ignored
        }

        BaseActivity.referenceCount--;
    }

    private void setUpDebugDrawer(@NonNull final Takt.Program program) {
        final SwitchAction headerSwitchAction   = new SwitchAction(this.getString(R.string.pref_header_image_enabled), enabled -> Settings.setHeaderImageEnabled(this, enabled));
        final SwitchAction panoramaSwitchAction = new SwitchAction(this.getString(R.string.pref_panorama_enabled), enabled -> Settings.setPanoramaEnabled(this, enabled));

        this.debugDrawer = new DebugDrawer.Builder(this)
            .modules(
                new ActionsModule(headerSwitchAction, panoramaSwitchAction),
                new FpsModule(program),
                new NetworkModule(this),
                new BuildModule(this),
                new SettingsModule(this),
                new DeviceModule(this)
            )
            .build();

        if (Settings.isHeaderImageEnabled(this)) headerSwitchAction.setChecked(true);
        if (Settings.isPanoramaEnabled(this)) panoramaSwitchAction.setChecked(true);
    }
}
