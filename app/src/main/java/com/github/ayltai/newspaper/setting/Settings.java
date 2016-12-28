package com.github.ayltai.newspaper.setting;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;

import com.facebook.common.internal.Sets;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.util.SuppressFBWarnings;

@SuppressFBWarnings("PMB_POSSIBLE_MEMORY_BLOAT")
public final class Settings {
    //region Constants

    public static final String PREF_COMPACT_LAYOUT = "PREF_COMPACT_LAYOUT";
    public static final String PREF_CATEGORIES     = "PREF_CATEGORIES";

    //endregion

    private static final Map<String, Integer> POSITIONS = new HashMap<>();

    private Settings() {
    }

    @Constants.ListViewType
    public static int getListViewType(@NonNull final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Settings.PREF_COMPACT_LAYOUT, true) ? Constants.LIST_VIEW_TYPE_COMPACT : Constants.LIST_VIEW_TYPE_COZY;
    }

    public static Set<String> getCategories(@NonNull final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getStringSet(Settings.PREF_CATEGORIES, Sets.newHashSet(context.getResources().getStringArray(R.array.pref_category_values)));
    }

    public static int getPosition(@NonNull final String url) {
        if (Settings.POSITIONS.containsKey(url)) return Settings.POSITIONS.get(url);

        return 0;
    }

    public static void setPosition(@NonNull final String url, final int position) {
        Settings.POSITIONS.put(url, position);
    }
}
