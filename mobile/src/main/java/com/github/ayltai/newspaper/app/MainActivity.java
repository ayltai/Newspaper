package com.github.ayltai.newspaper.app;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.config.UserConfig;
import com.github.ayltai.newspaper.data.DaggerDataComponent;
import com.github.ayltai.newspaper.data.DataManager;
import com.github.ayltai.newspaper.data.DataModule;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.util.RxUtils;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import io.reactivex.Single;
import io.realm.Realm;

public final class MainActivity extends AppCompatActivity {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private FlowController controller;
    private Realm          realm;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setTheme(UserConfig.getTheme(this) == Constants.THEME_LIGHT ? R.style.AppTheme_Light : R.style.AppTheme_Dark);

        Single.<Realm>create(emitter -> emitter.onSuccess(DaggerDataComponent.builder()
            .dataModule(new DataModule(this))
            .build()
            .realm()))
            .compose(RxUtils.applySingleSchedulers(DataManager.SCHEDULER))
            .subscribe(realm -> this.realm = realm);
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
}
