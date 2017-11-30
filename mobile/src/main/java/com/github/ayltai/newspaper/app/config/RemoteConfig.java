package com.github.ayltai.newspaper.app.config;

import javax.inject.Singleton;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.util.DevUtils;

@Singleton
public final class RemoteConfig {
    private final FirebaseRemoteConfig remoteConfig;

    RemoteConfig(@NonNull final Activity activity) {
        if (DevUtils.isRunningUnitTest()) {
            this.remoteConfig = null;
        } else {
            this.remoteConfig = FirebaseRemoteConfig.getInstance();
            this.remoteConfig.setConfigSettings(new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(DevUtils.isLoggable() && !DevUtils.isRunningTests())
                .build());

            this.remoteConfig.setDefaults(R.xml.config);

            this.remoteConfig.fetch(this.remoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled() ? 0 : Constants.REMOTE_CONFIG_CACHE_EXPIRATION)
                .addOnSuccessListener(activity, irrelevant -> remoteConfig.activateFetched())
                .addOnFailureListener(activity, error -> {
                    if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);
                });
        }
    }

    @Constants.ViewStyle
    public int getViewStyle() {
        return this.remoteConfig == null ? Constants.VIEW_STYLE_DEFAULT : this.remoteConfig.getLong("view_style") == Constants.VIEW_STYLE_COZY ? Constants.VIEW_STYLE_COZY : Constants.VIEW_STYLE_COMPACT;
    }

    @Constants.Theme
    public int getTheme() {
        return this.remoteConfig == null ? Constants.THEME_DEFAULT : this.remoteConfig.getLong("theme") == Constants.THEME_DARK ? Constants.THEME_DARK : Constants.THEME_LIGHT;
    }

    public boolean isPanoramaEnabled() {
        return this.remoteConfig == null ? Constants.PANORAMA_DEFAULT : this.remoteConfig.getBoolean("panorama");
    }
}
