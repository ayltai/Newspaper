package com.github.ayltai.newspaper.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;

public final class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setTheme(
            ComponentFactory.getInstance()
                .getConfigComponent(this)
                .userConfig()
                .getTheme() == Constants.THEME_LIGHT
            ? R.style.AppSplashThemeLight
            : R.style.AppSplashThemeDark
        );

        this.startActivity(new Intent(this, MainActivity.class));
        this.finish();
    }
}
