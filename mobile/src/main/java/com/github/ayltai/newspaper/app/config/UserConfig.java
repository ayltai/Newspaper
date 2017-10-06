package com.github.ayltai.newspaper.app.config;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.inject.Singleton;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.util.Sets;

@Singleton
public final class UserConfig {
    //region Constants

    private static final String KEY_SOURCES    = "sources";
    private static final String KEY_CATEGORIES = "categories";
    private static final String KEY_VIEW_STYLE = "viewStyle";
    private static final String KEY_THEME      = "theme";
    private static final String KEY_AUTO_PLAY  = "autoPlay";
    private static final String KEY_PANORAMA   = "panorama";

    //endregion

    private final Context      context;
    private final RemoteConfig remoteConfig;

    UserConfig(@NonNull final Context context, @NonNull final RemoteConfig remoteConfig) {
        this.context      = context.getApplicationContext();
        this.remoteConfig = remoteConfig;
    }

    @NonNull
    public Set<String> getSources() {
        final Set<String> sources = PreferenceManager.getDefaultSharedPreferences(this.context).getStringSet(UserConfig.KEY_SOURCES, null);
        return sources == null || sources.isEmpty() ? this.getDefaultSources() : sources;
    }

    public void setSources(@NonNull final Set<String> sources) {
        PreferenceManager.getDefaultSharedPreferences(this.context).edit().putStringSet(UserConfig.KEY_SOURCES, sources).apply();
    }

    @NonNull
    public Set<String> getDefaultSources() {
        return Sets.from(this.context.getResources().getStringArray(R.array.sources));
    }

    @NonNull
    public List<String> getCategories() {
        final String json = PreferenceManager.getDefaultSharedPreferences(this.context).getString(UserConfig.KEY_CATEGORIES, null);
        if (TextUtils.isEmpty(json)) return this.getDefaultCategories();

        return new Gson().fromJson(json, new TypeToken<List<String>>() {}.getType());
    }

    public void setCategories(@NonNull final List<String> categories) {
        PreferenceManager.getDefaultSharedPreferences(this.context).edit().putString(UserConfig.KEY_CATEGORIES, new Gson().toJson(categories)).apply();
    }

    @NonNull
    public List<String> getDefaultCategories() {
        return Arrays.asList(this.context.getResources().getStringArray(R.array.categories));
    }

    @SuppressWarnings("WrongConstant")
    @Constants.ViewStyle
    public int getViewStyle() {
        return PreferenceManager.getDefaultSharedPreferences(this.context).getInt(UserConfig.KEY_VIEW_STYLE, this.remoteConfig.getViewStyle());
    }

    public void setViewStyle(@Constants.ViewStyle final int viewStyle) {
        PreferenceManager.getDefaultSharedPreferences(this.context).edit().putInt(UserConfig.KEY_VIEW_STYLE, viewStyle).apply();
    }

    @SuppressWarnings("WrongConstant")
    @Constants.Theme
    public int getTheme() {
        return PreferenceManager.getDefaultSharedPreferences(this.context).getInt(UserConfig.KEY_THEME, this.remoteConfig.getTheme());
    }

    public void setTheme(@Constants.Theme final int theme) {
        PreferenceManager.getDefaultSharedPreferences(this.context).edit().putInt(UserConfig.KEY_THEME, theme).apply();
    }

    public boolean isAutoPlayEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean(UserConfig.KEY_AUTO_PLAY, Constants.AUTO_PLAY_DEFAULT);
    }

    public void setAutoPlayEnabled(final boolean enabled) {
        PreferenceManager.getDefaultSharedPreferences(this.context).edit().putBoolean(UserConfig.KEY_AUTO_PLAY, enabled).apply();
    }

    public boolean isPanoramaEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean(UserConfig.KEY_PANORAMA, this.remoteConfig.isPanoramaEnabled());
    }

    public void setPanoramaEnabled(final boolean enabled) {
        PreferenceManager.getDefaultSharedPreferences(this.context).edit().putBoolean(UserConfig.KEY_PANORAMA, enabled).apply();
    }
}
