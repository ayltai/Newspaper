package com.github.ayltai.newspaper.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.collection.ArraySet;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.data.model.Category;
import com.github.ayltai.newspaper.data.model.Source;

@Singleton
public final class PreferenceUserConfigs implements UserConfigs {
    //region Constants

    private static final String KEY_CONNECT_TIMEOUT = "CONNECT_TIMEOUT";
    private static final String KEY_READ_TIMEOUT    = "READ_TIMEOUT";
    private static final String KEY_WRITE_TIMEOUT   = "WRITE_TIMEOUT";
    private static final String KEY_THEME           = "THEME";
    private static final String KEY_STYLE           = "STYLE";
    private static final String KEY_SOURCE_NAMES    = "SOURCE_NAMES";
    private static final String KEY_CATEGORY_NAMES  = "CATEGORY_NAMES";

    private static final int DEFAULT_CONNECT_TIMEOUT = 10;
    private static final int DEFAULT_READ_TIMEOUT    = 30;
    private static final int DEFAULT_WRITE_TIMEOUT   = 30;

    //endregion

    private static PreferenceUserConfigs instance;

    @Nonnull
    @NonNull
    private final SharedPreferences preferences;

    static void init(@Nonnull @NonNull @lombok.NonNull final Context context) {
        if (PreferenceUserConfigs.instance == null) PreferenceUserConfigs.instance = new PreferenceUserConfigs(context);
    }

    @Nonnull
    @NonNull
    static UserConfigs getInstance() {
        if (PreferenceUserConfigs.instance == null) throw new NullPointerException("Did you forget to call init()?");

        return PreferenceUserConfigs.instance;
    }

    private PreferenceUserConfigs(@Nonnull @NonNull @lombok.NonNull final Context context) {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public int getConnectTimeout() {
        return this.preferences.getInt(PreferenceUserConfigs.KEY_CONNECT_TIMEOUT, PreferenceUserConfigs.DEFAULT_CONNECT_TIMEOUT);
    }

    @Override
    public int getReadTimeout() {
        return this.preferences.getInt(PreferenceUserConfigs.KEY_READ_TIMEOUT, PreferenceUserConfigs.DEFAULT_READ_TIMEOUT);
    }

    @Override
    public int getWriteTimeout() {
        return this.preferences.getInt(PreferenceUserConfigs.KEY_WRITE_TIMEOUT, PreferenceUserConfigs.DEFAULT_WRITE_TIMEOUT);
    }

    @Constants.Theme
    @Override
    public int getTheme() {
        return this.preferences.getInt(PreferenceUserConfigs.KEY_THEME, Constants.THEME_LIGHT);
    }

    @Override
    public void setTheme(final int theme) {
        this.preferences
            .edit()
            .putInt(PreferenceUserConfigs.KEY_THEME, theme)
            .commit();
    }

    @Constants.Style
    @Override
    public int getStyle() {
        return this.preferences.getInt(PreferenceUserConfigs.KEY_STYLE, Constants.STYLE_COMFORTABLE);
    }

    @Override
    public void setStyle(final int style) {
        this.preferences
            .edit()
            .putInt(PreferenceUserConfigs.KEY_STYLE, style)
            .commit();
    }

    @Nonnull
    @NonNull
    @Override
    public List<String> getSourceNames() {
        return new ArrayList<>(this.preferences.getStringSet(PreferenceUserConfigs.KEY_SOURCE_NAMES, new ArraySet<>(Source.DEFAULT_SOURCES)));
    }

    @Override
    public void setSourceNames(@NonNull @Nonnull final List<String> sourceNames) {
        this.preferences
            .edit()
            .putStringSet(PreferenceUserConfigs.KEY_SOURCE_NAMES, new HashSet<>(sourceNames))
            .commit();
    }

    @Nonnull
    @NonNull
    @Override
    public List<String> getCategoryNames() {
        return new ArrayList<>(this.preferences.getStringSet(PreferenceUserConfigs.KEY_CATEGORY_NAMES, new ArraySet<>(Category.DEFAULT_CATEGORIES)));
    }

    @Override
    public void setCategoryNames(@NonNull @Nonnull final List<String> categoryNames) {
        this.preferences
            .edit()
            .putStringSet(PreferenceUserConfigs.KEY_CATEGORY_NAMES, new HashSet<>(categoryNames))
            .commit();
    }
}
