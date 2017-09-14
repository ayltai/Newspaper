package com.github.ayltai.newspaper.config;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.util.Sets;

public final class UserConfig {
    private static final String KEY_SOURCES    = "sources";
    private static final String KEY_CATEGORIES = "categories";

    private UserConfig() {
    }

    @NonNull
    public static Set<String> getSources(@NonNull final Context context) {
        final Set<String> sources = PreferenceManager.getDefaultSharedPreferences(context).getStringSet(UserConfig.KEY_SOURCES, null);
        return sources == null ? UserConfig.getDefaultSources(context) : sources;
    }

    public static void setSources(@NonNull final Context context, @NonNull final Set<String> sources) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putStringSet(UserConfig.KEY_SOURCES, sources).apply();
    }

    @NonNull
    public static Set<String> getDefaultSources(@NonNull final Context context) {
        return Sets.from(context.getResources().getStringArray(R.array.sources));
    }

    @NonNull
    public static List<String> getCategories(@NonNull final Context context) {
        final String json = PreferenceManager.getDefaultSharedPreferences(context).getString(UserConfig.KEY_CATEGORIES, null);
        if (json == null) return UserConfig.getDefaultCategories(context);

        return new Gson().fromJson(json, new TypeToken<List<String>>() {}.getType());
    }

    public static void setCategories(@NonNull final Context context, @NonNull final List<String> categories) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(UserConfig.KEY_CATEGORIES, new Gson().toJson(categories)).apply();
    }

    @NonNull
    public static List<String> getDefaultCategories(@NonNull final Context context) {
        return Arrays.asList(context.getResources().getStringArray(R.array.categories));
    }
}
