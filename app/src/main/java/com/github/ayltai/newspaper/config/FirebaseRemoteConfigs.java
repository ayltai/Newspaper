package com.github.ayltai.newspaper.config;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.util.RxUtils;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

@Singleton
public final class FirebaseRemoteConfigs implements RemoteConfigs {
    private static final int CACHE_EXPIRATION = 30 * 60 * 1000;

    private static FirebaseRemoteConfigs instance;

    private final FirebaseRemoteConfig remoteConfig;

    static void init(@Nonnull @NonNull @lombok.NonNull final Activity activity) {
        if (FirebaseRemoteConfigs.instance == null) FirebaseRemoteConfigs.instance = new FirebaseRemoteConfigs(activity);
    }

    @Nonnull
    @NonNull
    static RemoteConfigs getInstance() {
        return FirebaseRemoteConfigs.instance;
    }

    private FirebaseRemoteConfigs(@Nonnull @NonNull @lombok.NonNull final Activity activity) {
        if (DevUtils.isRunningUnitTest()) {
            this.remoteConfig = null;
        } else {
            this.remoteConfig = FirebaseRemoteConfig.getInstance();
            this.remoteConfig.setConfigSettings(new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(DevUtils.isLoggable() && !DevUtils.isRunningUnitTest())
                .build());

            this.remoteConfig.setDefaults(R.xml.configs);

            this.remoteConfig.fetch(this.remoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled() ? 0 : FirebaseRemoteConfigs.CACHE_EXPIRATION)
                .addOnSuccessListener(activity, irrelevant -> this.remoteConfig.activateFetched())
                .addOnFailureListener(activity, RxUtils::handleError);
        }
    }

    @Constants.Theme
    @Override
    public int getTheme() {
        return this.remoteConfig == null ? Constants.THEME_LIGHT : this.remoteConfig.getLong("theme") == Constants.THEME_DARK ? Constants.THEME_DARK : Constants.THEME_LIGHT;
    }

    @Constants.Style
    @Override
    public int getStyle() {
        return this.remoteConfig == null ? Constants.STYLE_COMFORTABLE : this.remoteConfig.getLong("style") == Constants.STYLE_COMPACT ? Constants.STYLE_COMPACT : Constants.STYLE_COMFORTABLE;
    }
}
