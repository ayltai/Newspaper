package com.github.ayltai.newspaper.setting;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.util.ContextUtils;

public final class SettingsActivity extends AppCompatActivity {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        ContextUtils.setSettingsTheme(this);

        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_settings);

        final Toolbar toolbar = (Toolbar)this.findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);

        final ActionBar actionBar = this.getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(R.string.action_settings);
            actionBar.setHomeAsUpIndicator(null);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        this.setResult(Activity.RESULT_CANCELED);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.onBackPressed();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
